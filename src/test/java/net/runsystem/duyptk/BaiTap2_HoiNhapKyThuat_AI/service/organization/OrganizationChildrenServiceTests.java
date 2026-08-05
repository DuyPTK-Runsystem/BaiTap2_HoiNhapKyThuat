package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.service.organization;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.responseDTO.ResItemDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.Folder;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.ItemType;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.User;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.VocabSet;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository.FolderRepository;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository.ItemRepository;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository.UserRepository;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository.VocabSetRepository;

class OrganizationChildrenServiceTests {
    private static final Long FOLDER_ID = 10L;
    private static final Long USER_ID = 1L;
    private static final String EMAIL = "learner@example.com";

    private final UserRepository userRepository = Mockito.mock(UserRepository.class);
    private final ItemRepository itemRepository = Mockito.mock(ItemRepository.class);
    private final FolderRepository folderRepository = Mockito.mock(FolderRepository.class);
    private final VocabSetRepository vocabSetRepository = Mockito.mock(VocabSetRepository.class);
    private final OrganizationService organizationService = new OrganizationService(
            userRepository,
            itemRepository,
            folderRepository,
            vocabSetRepository);

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnRootItemsWhenParentIdIsMissing() {
        mockCurrentUser();
        Mockito.when(itemRepository.findByUserIdAndParentIsNullOrderByIdAsc(USER_ID))
                .thenReturn(List.of(folder(11L, "IELTS", null), vocabSet(12L, "Root verbs", null)));

        List<ResItemDTO> response = organizationService.getChildren(null);

        Assertions.assertThat(response)
                .extracting(ResItemDTO::getId, ResItemDTO::getType, ResItemDTO::getParentId)
                .containsExactly(
                        Assertions.tuple(11L, ItemType.FOLDER, null),
                        Assertions.tuple(12L, ItemType.VOCAB_SET, null));
    }

    @Test
    void shouldReturnDirectChildrenWhenParentFolderBelongsToCurrentUser() {
        mockCurrentUser();
        Folder parent = folder(FOLDER_ID, "IELTS", null);
        Mockito.when(folderRepository.findByIdAndUserId(FOLDER_ID, USER_ID)).thenReturn(Optional.of(parent));
        Mockito.when(itemRepository.findByUserIdAndParentIdOrderByIdAsc(USER_ID, FOLDER_ID))
                .thenReturn(List.of(folder(13L, "Listening", parent), vocabSet(14L, "Band 7 words", parent)));

        List<ResItemDTO> response = organizationService.getChildren(FOLDER_ID);

        Assertions.assertThat(response)
                .extracting(ResItemDTO::getId, ResItemDTO::getType, ResItemDTO::getParentId)
                .containsExactly(
                        Assertions.tuple(13L, ItemType.FOLDER, FOLDER_ID),
                        Assertions.tuple(14L, ItemType.VOCAB_SET, FOLDER_ID));
    }

    @Test
    void shouldRejectParentThatIsMissingOrNotOwnedByCurrentUser() {
        mockCurrentUser();
        Mockito.when(folderRepository.findByIdAndUserId(FOLDER_ID, USER_ID)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> organizationService.getChildren(FOLDER_ID))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Folder cha không tồn tại hoặc không thuộc người dùng hiện tại");
    }

    private void mockCurrentUser() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(EMAIL, "n/a"));
        Mockito.when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(currentUser()));
    }

    private User currentUser() {
        return User.builder()
                .id(USER_ID)
                .email(EMAIL)
                .password("hashed")
                .build();
    }

    private Folder folder(Long id, String name, Folder parent) {
        return Folder.builder()
                .id(id)
                .folderName(name)
                .parent(parent)
                .user(currentUser())
                .build();
    }

    private VocabSet vocabSet(Long id, String name, Folder parent) {
        return VocabSet.builder()
                .id(id)
                .vocabSetName(name)
                .parent(parent)
                .user(currentUser())
                .build();
    }
}
