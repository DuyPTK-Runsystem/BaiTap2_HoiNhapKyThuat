package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.service.organization;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.Folder;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository.FolderRepository;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository.VocabSetRepository;

@Service
@RequiredArgsConstructor
public class OrganizationItemNameValidationService {
    private final FolderRepository folderRepository;
    private final VocabSetRepository vocabSetRepository;

    public void validateUniqueSiblingName(Long userId, Folder parent, String itemName) {
        boolean exists = parent == null
                ? hasRootSiblingName(userId, itemName)
                : hasChildSiblingName(userId, parent.getId(), itemName);
        if (exists) {
            throw new IllegalArgumentException("Tên item đã tồn tại trong cùng thư mục");
        }
    }

    private boolean hasRootSiblingName(Long userId, String itemName) {
        return !folderRepository.findByUserIdAndParentIsNullAndFolderName(userId, itemName).isEmpty()
                || !vocabSetRepository.findByUserIdAndParentIsNullAndVocabSetName(userId, itemName).isEmpty();
    }

    private boolean hasChildSiblingName(Long userId, Long parentId, String itemName) {
        return !folderRepository.findByUserIdAndParentIdAndFolderName(userId, parentId, itemName).isEmpty()
                || !vocabSetRepository.findByUserIdAndParentIdAndVocabSetName(userId, parentId, itemName).isEmpty();
    }
}
