package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.service.organization;

import java.util.List;
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
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository.UserRepository;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository.VocabSetRepository;

class OrganizationItemSearchServiceTests {
    private static final Long ROOT_ID = 10L;
    private static final Long UNIT_ID = 11L;
    private static final Long VOCAB_SET_ID = 12L;
    private static final Long USER_ID = 1L;
    private static final String COMMON_VERBS = "Common verbs";
    private static final String EMAIL = "learner@example.com";
    private static final String IELTS = "IELTS";
    private static final String ITEM_PATH = "/IELTS/Unit 1/Common verbs";
    private static final String KEYWORD = "verb";
    private static final String UNIT_1 = "Unit 1";

    private final UserRepository userRepository = Mockito.mock(UserRepository.class);
    private final FolderRepository folderRepository = Mockito.mock(FolderRepository.class);
    private final VocabSetRepository vocabSetRepository = Mockito.mock(VocabSetRepository.class);
    private final OrganizationItemLookupService organizationItemLookupService = new OrganizationItemLookupService(
            userRepository,
            folderRepository,
            vocabSetRepository);

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void searchItemsShouldReturnOwnedItemsWithPath() {
        mockCurrentUser();
        Folder root = folder(ROOT_ID, IELTS, null);
        Folder unit = folder(UNIT_ID, UNIT_1, root);
        VocabSet vocabSet = vocabSet(VOCAB_SET_ID, COMMON_VERBS, unit);
        Mockito.when(folderRepository.findByUserIdAndFolderNameContainingIgnoreCaseOrderByIdAsc(USER_ID, KEYWORD))
                .thenReturn(List.of());
        Mockito.when(vocabSetRepository.findByUserIdAndVocabSetNameContainingIgnoreCaseOrderByIdAsc(USER_ID, KEYWORD))
                .thenReturn(List.of(vocabSet));

        List<ResItemDTO> response = organizationItemLookupService.searchItems(" " + KEYWORD + " ");

        Assertions.assertThat(response)
                .extracting(ResItemDTO::getId, ResItemDTO::getType, ResItemDTO::getName, ResItemDTO::getItemPath)
                .containsExactly(Assertions.tuple(VOCAB_SET_ID, ItemType.VOCAB_SET, COMMON_VERBS, ITEM_PATH));
    }

    @Test
    void searchItemsShouldRejectBlankName() {
        mockCurrentUser();

        Assertions.assertThatThrownBy(() -> organizationItemLookupService.searchItems(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Từ khóa tìm kiếm không được để trống");
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
