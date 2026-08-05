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
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository.UserRepository;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository.VocabSetRepository;

class OrganizationItemPathServiceTests {
    private static final Long ROOT_ID = 10L;
    private static final Long UNIT_ID = 11L;
    private static final Long VOCAB_SET_ID = 12L;
    private static final Long USER_ID = 1L;
    private static final String COMMON_VERBS = "Common verbs";
    private static final String EMAIL = "learner@example.com";
    private static final String IELTS = "IELTS";
    private static final String ITEM_PATH = "/IELTS/Unit 1/Common verbs";
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
    void resolveItemByPathShouldReturnNestedItemWithPath() {
        mockCurrentUser();
        Folder root = folder(ROOT_ID, IELTS, null);
        Folder unit = folder(UNIT_ID, UNIT_1, root);
        VocabSet vocabSet = vocabSet(VOCAB_SET_ID, COMMON_VERBS, unit);
        mockPathSegment(null, IELTS, List.of(root), List.of());
        mockPathSegment(ROOT_ID, UNIT_1, List.of(unit), List.of());
        mockPathSegment(UNIT_ID, COMMON_VERBS, List.of(), List.of(vocabSet));

        ResItemDTO response = organizationItemLookupService.getItemByPath(ITEM_PATH + "/");

        Assertions.assertThat(List.of(
                response.getId(),
                response.getType(),
                response.getParentId(),
                response.getItemPath()))
                .containsExactly(VOCAB_SET_ID, ItemType.VOCAB_SET, UNIT_ID, ITEM_PATH);
    }

    @Test
    void resolveItemByPathShouldRejectBlankSegment() {
        mockCurrentUser();

        Assertions.assertThatThrownBy(() -> organizationItemLookupService.getItemByPath("/IELTS//Unit 1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Path không được chứa segment trống");
    }

    @Test
    void resolveItemByPathShouldRejectAmbiguousSiblingName() {
        mockCurrentUser();
        mockPathSegment(null, IELTS, List.of(folder(ROOT_ID, IELTS, null)),
                List.of(vocabSet(VOCAB_SET_ID, IELTS, null)));

        Assertions.assertThatThrownBy(() -> organizationItemLookupService.getItemByPath("/" + IELTS))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Item path không đủ rõ do có nhiều item cùng tên trong cùng một thư mục");
    }

    @Test
    void resolveItemByPathShouldRejectPathUnderVocabSet() {
        mockCurrentUser();
        mockPathSegment(null, COMMON_VERBS, List.of(), List.of(vocabSet(VOCAB_SET_ID, COMMON_VERBS, null)));

        Assertions.assertThatThrownBy(() -> organizationItemLookupService.getItemByPath("/Common verbs/Lesson A"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Vocab set không chứa item con");
    }

    @Test
    void resolveItemByPathShouldRejectMissingPath() {
        mockCurrentUser();
        mockPathSegment(null, IELTS, List.of(), List.of());

        Assertions.assertThatThrownBy(() -> organizationItemLookupService.getItemByPath("/" + IELTS))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Item path không tồn tại hoặc không thuộc người dùng hiện tại");
    }

    private void mockCurrentUser() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(EMAIL, "n/a"));
        User user = User.builder()
                .id(USER_ID)
                .email(EMAIL)
                .password("hashed")
                .build();
        Mockito.when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
    }

    private void mockPathSegment(Long parentId, String segment, List<Folder> folders, List<VocabSet> vocabSets) {
        if (parentId == null) {
            Mockito.when(folderRepository.findByUserIdAndParentIsNullAndFolderName(USER_ID, segment))
                    .thenReturn(folders);
            Mockito.when(vocabSetRepository.findByUserIdAndParentIsNullAndVocabSetName(USER_ID, segment))
                    .thenReturn(vocabSets);
            return;
        }

        Mockito.when(folderRepository.findByUserIdAndParentIdAndFolderName(USER_ID, parentId, segment))
                .thenReturn(folders);
        Mockito.when(vocabSetRepository.findByUserIdAndParentIdAndVocabSetName(USER_ID, parentId, segment))
                .thenReturn(vocabSets);
    }

    private Folder folder(Long id, String name, Folder parent) {
        return Folder.builder()
                .id(id)
                .folderName(name)
                .parent(parent)
                .build();
    }

    private VocabSet vocabSet(Long id, String name, Folder parent) {
        return VocabSet.builder()
                .id(id)
                .vocabSetName(name)
                .parent(parent)
                .build();
    }
}
