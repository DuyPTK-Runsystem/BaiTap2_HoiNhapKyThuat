package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.service.organization;

import java.util.List;
import java.util.NoSuchElementException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.requestDTO.ReqCreateFolderDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.requestDTO.ReqCreateVocabSetDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.responseDTO.ResItemDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.Folder;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.Item;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.ItemType;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.User;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.VocabSet;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository.FolderRepository;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository.ItemRepository;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository.UserRepository;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository.VocabSetRepository;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.util.SecurityUtil;

@Service
@RequiredArgsConstructor
public class OrganizationService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final FolderRepository folderRepository;
    private final VocabSetRepository vocabSetRepository;
    private final OrganizationItemNameValidationService organizationItemNameValidationService;

    @Transactional
    public ResItemDTO createFolder(ReqCreateFolderDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Request tạo thư mục không được để trống");
        }

        User user = currentUser();
        Folder parent = resolveParentFolder(request.getParentId(), user.getId());
        String folderName = requireName(request.getFolderName(), "Tên thư mục không được để trống");
        organizationItemNameValidationService.validateUniqueSiblingName(user.getId(), parent, folderName);
        Folder folder = Folder.builder()
                .folderName(folderName)
                .parent(parent)
                .user(user)
                .build();

        return convertToDTO(folderRepository.save(folder));
    }

    @Transactional
    public ResItemDTO createVocabSet(ReqCreateVocabSetDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Request tạo tập từ vựng không được để trống");
        }

        User user = currentUser();
        Folder parent = resolveParentFolder(request.getParentId(), user.getId());
        String vocabSetName = requireName(request.getVocabSetName(), "Tên tập từ vựng không được để trống");
        organizationItemNameValidationService.validateUniqueSiblingName(user.getId(), parent, vocabSetName);
        VocabSet vocabSet = VocabSet.builder()
                .vocabSetName(vocabSetName)
                .vocabSetDescription(blankToNull(request.getVocabSetDescription()))
                .parent(parent)
                .user(user)
                .build();

        return convertToDTO(vocabSetRepository.save(vocabSet));
    }

    @Transactional(readOnly = true)
    public List<ResItemDTO> getChildren(Long parentId) {
        User user = currentUser();
        if (parentId == null) {
            return itemRepository.findByUserIdAndParentIsNullOrderByIdAsc(user.getId()).stream()
                    .map(this::convertToDTO)
                    .toList();
        }

        resolveParentFolder(parentId, user.getId());
        return itemRepository.findByUserIdAndParentIdOrderByIdAsc(user.getId(), parentId).stream()
                .map(this::convertToDTO)
                .toList();
    }

    public ResItemDTO convertToDTO(Item item) {
        if (item == null) {
            return null;
        }

        Long parentId = item.getParent() == null ? null : item.getParent().getId();
        if (item instanceof Folder folder) {
            return ResItemDTO.builder()
                    .id(folder.getId())
                    .type(ItemType.FOLDER)
                    .name(folder.getFolderName())
                    .parentId(parentId)
                    .build();
        }
        if (item instanceof VocabSet vocabSet) {
            return ResItemDTO.builder()
                    .id(vocabSet.getId())
                    .type(ItemType.VOCAB_SET)
                    .name(vocabSet.getVocabSetName())
                    .description(vocabSet.getVocabSetDescription())
                    .parentId(parentId)
                    .vocabCount(vocabSet.getVocabs().size())
                    .build();
        }

        return ResItemDTO.builder()
                .id(item.getId())
                .type(item.getType())
                .parentId(parentId)
                .build();
    }

    private User currentUser() {
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy người dùng hiện tại"));
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy người dùng hiện tại"));
    }

    private Folder resolveParentFolder(Long parentId, Long userId) {
        if (parentId == null) {
            return null;
        }
        return folderRepository.findByIdAndUserId(parentId, userId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Folder cha không tồn tại hoặc không thuộc người dùng hiện tại"));
    }

    private String requireName(String value, String message) {
        String normalizedValue = blankToNull(value);
        if (normalizedValue == null) {
            throw new IllegalArgumentException(message);
        }
        return normalizedValue;
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
