package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.service.organization;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.requestDTO.ReqCreateVocabSetDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.responseDTO.ResItemDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.Folder;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.ItemType;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.User;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.VocabSet;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository.FolderRepository;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository.ItemRepository;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository.UserRepository;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository.VocabSetRepository;

class OrganizationVocabSetServiceTests {
    private static final Long USER_ID = 1L;
    private static final Long PARENT_ID = 10L;
    private static final String EMAIL = "learner@example.com";
    private static final String VOCAB_SET_DESCRIPTION = "Basic daily verbs";
    private static final String VOCAB_SET_NAME = "Common verbs";

    private final UserRepository userRepository = Mockito.mock(UserRepository.class);
    private final ItemRepository itemRepository = Mockito.mock(ItemRepository.class);
    private final FolderRepository folderRepository = Mockito.mock(FolderRepository.class);
    private final VocabSetRepository vocabSetRepository = Mockito.mock(VocabSetRepository.class);
    private final OrganizationItemNameValidationService organizationItemNameValidationService =
            Mockito.mock(OrganizationItemNameValidationService.class);
    private final OrganizationService organizationService = new OrganizationService(
            userRepository,
            itemRepository,
            folderRepository,
            vocabSetRepository,
            organizationItemNameValidationService);

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createVocabSetShouldCreateRootVocabSetForCurrentUser() {
        mockCurrentUser();
        Mockito.when(vocabSetRepository.save(ArgumentMatchers.any(VocabSet.class)))
                .thenAnswer(invocation -> vocabSetWithId(invocation.getArgument(0), 20L));

        ResItemDTO response = organizationService.createVocabSet(ReqCreateVocabSetDTO.builder()
                .vocabSetName(VOCAB_SET_NAME)
                .vocabSetDescription(VOCAB_SET_DESCRIPTION)
                .build());

        Assertions.assertThat(response)
                .extracting(
                        ResItemDTO::getId,
                        ResItemDTO::getType,
                        ResItemDTO::getName,
                        ResItemDTO::getDescription,
                        ResItemDTO::getParentId,
                        ResItemDTO::getVocabCount)
                .containsExactly(20L, ItemType.VOCAB_SET, VOCAB_SET_NAME, VOCAB_SET_DESCRIPTION, null, 0);
    }

    @Test
    void createVocabSetShouldUseParentFolderWhenParentBelongsToCurrentUser() {
        mockCurrentUser();
        Folder parent = parentFolder();
        Mockito.when(folderRepository.findByIdAndUserId(PARENT_ID, USER_ID)).thenReturn(Optional.of(parent));
        Mockito.when(vocabSetRepository.save(ArgumentMatchers.any(VocabSet.class)))
                .thenAnswer(invocation -> vocabSetWithId(invocation.getArgument(0), 21L));

        ResItemDTO response = organizationService.createVocabSet(ReqCreateVocabSetDTO.builder()
                .vocabSetName(VOCAB_SET_NAME)
                .parentId(PARENT_ID)
                .build());

        Assertions.assertThat(response.getParentId()).isEqualTo(PARENT_ID);
    }

    @Test
    void createVocabSetShouldRejectMissingVocabSetName() {
        mockCurrentUser();

        Assertions.assertThatThrownBy(() -> organizationService.createVocabSet(ReqCreateVocabSetDTO.builder()
                .vocabSetName(" ")
                .build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tên tập từ vựng không được để trống");
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

    private Folder parentFolder() {
        return Folder.builder()
                .id(PARENT_ID)
                .folderName("Parent")
                .user(currentUser())
                .build();
    }

    private VocabSet vocabSetWithId(VocabSet vocabSet, Long id) {
        vocabSet.setId(id);
        return vocabSet;
    }
}
