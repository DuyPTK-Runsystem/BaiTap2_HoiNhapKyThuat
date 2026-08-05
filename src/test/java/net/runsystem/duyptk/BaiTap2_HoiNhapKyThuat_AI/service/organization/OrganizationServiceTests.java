package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.service.organization;

import java.util.NoSuchElementException;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.requestDTO.ReqCreateFolderDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.responseDTO.ResItemDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.Folder;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.ItemType;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.User;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository.FolderRepository;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository.ItemRepository;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository.UserRepository;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository.VocabSetRepository;

class OrganizationServiceTests {
    private static final Long USER_ID = 1L;
    private static final Long PARENT_ID = 10L;
    private static final String EMAIL = "learner@example.com";
    private static final String FOLDER_NAME = "Unit 1";

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
    void createFolderShouldCreateRootFolderForCurrentUser() {
        mockCurrentUser();
        Mockito.when(folderRepository.save(ArgumentMatchers.any(Folder.class)))
                .thenAnswer(invocation -> folderWithId(invocation.getArgument(0), 11L));

        ResItemDTO response = organizationService.createFolder(ReqCreateFolderDTO.builder()
                .folderName("  " + FOLDER_NAME + "  ")
                .build());

        Assertions.assertThat(response)
                .extracting(ResItemDTO::getId, ResItemDTO::getType, ResItemDTO::getName, ResItemDTO::getParentId)
                .containsExactly(11L, ItemType.FOLDER, FOLDER_NAME, null);
    }

    @Test
    void createFolderShouldUseParentFolderWhenParentBelongsToCurrentUser() {
        mockCurrentUser();
        Folder parent = parentFolder();
        Mockito.when(folderRepository.findByIdAndUserId(PARENT_ID, USER_ID)).thenReturn(Optional.of(parent));
        Mockito.when(folderRepository.save(ArgumentMatchers.any(Folder.class)))
                .thenAnswer(invocation -> folderWithId(invocation.getArgument(0), 12L));

        ResItemDTO response = organizationService.createFolder(ReqCreateFolderDTO.builder()
                .folderName(FOLDER_NAME)
                .parentId(PARENT_ID)
                .build());

        Assertions.assertThat(response.getParentId()).isEqualTo(PARENT_ID);
    }

    @Test
    void createFolderShouldRejectMissingFolderName() {
        mockCurrentUser();

        Assertions.assertThatThrownBy(() -> organizationService.createFolder(ReqCreateFolderDTO.builder()
                .folderName(" ")
                .build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tên thư mục không được để trống");
    }

    @Test
    void createFolderShouldRejectParentThatDoesNotBelongToCurrentUser() {
        mockCurrentUser();
        Mockito.when(folderRepository.findByIdAndUserId(PARENT_ID, USER_ID)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> organizationService.createFolder(ReqCreateFolderDTO.builder()
                .folderName(FOLDER_NAME)
                .parentId(PARENT_ID)
                .build()))
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

    private Folder parentFolder() {
        return Folder.builder()
                .id(PARENT_ID)
                .folderName("Parent")
                .user(currentUser())
                .build();
    }

    private Folder folderWithId(Folder folder, Long id) {
        folder.setId(id);
        return folder;
    }
}
