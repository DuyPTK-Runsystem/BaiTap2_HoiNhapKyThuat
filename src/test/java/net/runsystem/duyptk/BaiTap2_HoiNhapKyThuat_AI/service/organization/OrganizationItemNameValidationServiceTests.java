package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.service.organization;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.Folder;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.VocabSet;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository.FolderRepository;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository.VocabSetRepository;

class OrganizationItemNameValidationServiceTests {
    private static final Long PARENT_ID = 10L;
    private static final Long USER_ID = 1L;
    private static final String ITEM_NAME = "Unit 1";

    private final FolderRepository folderRepository = Mockito.mock(FolderRepository.class);
    private final VocabSetRepository vocabSetRepository = Mockito.mock(VocabSetRepository.class);
    private final OrganizationItemNameValidationService validationService =
            new OrganizationItemNameValidationService(folderRepository, vocabSetRepository);

    @Test
    void validateUniqueSiblingNameShouldRejectDuplicateRootVocabSetName() {
        Mockito.when(vocabSetRepository.findByUserIdAndParentIsNullAndVocabSetName(USER_ID, ITEM_NAME))
                .thenReturn(List.of(vocabSet(ITEM_NAME)));

        Assertions.assertThatThrownBy(() -> validationService.validateUniqueSiblingName(USER_ID, null, ITEM_NAME))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tên item đã tồn tại trong cùng thư mục");
    }

    @Test
    void validateUniqueSiblingNameShouldRejectDuplicateChildFolderName() {
        Folder parent = folder(PARENT_ID, "Parent", null);
        Mockito.when(folderRepository.findByUserIdAndParentIdAndFolderName(USER_ID, PARENT_ID, ITEM_NAME))
                .thenReturn(List.of(folder(11L, ITEM_NAME, parent)));

        Assertions.assertThatThrownBy(() -> validationService.validateUniqueSiblingName(USER_ID, parent, ITEM_NAME))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tên item đã tồn tại trong cùng thư mục");
    }

    private Folder folder(Long id, String name, Folder parent) {
        return Folder.builder()
                .id(id)
                .folderName(name)
                .parent(parent)
                .build();
    }

    private VocabSet vocabSet(String name) {
        return VocabSet.builder()
                .id(12L)
                .vocabSetName(name)
                .build();
    }
}
