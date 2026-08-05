package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.service.organization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.responseDTO.ResItemDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.Folder;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.Item;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.ItemType;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.User;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.VocabSet;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository.FolderRepository;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository.UserRepository;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository.VocabSetRepository;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.util.SecurityUtil;

@Service
@RequiredArgsConstructor
public class OrganizationItemLookupService {
    private final UserRepository userRepository;
    private final FolderRepository folderRepository;
    private final VocabSetRepository vocabSetRepository;

    @Transactional(readOnly = true)
    public List<ResItemDTO> searchItems(String name) {
        User user = currentUser();
        String keyword = normalizeSearchName(name);
        List<Item> items = new ArrayList<>();
        items.addAll(folderRepository.findByUserIdAndFolderNameContainingIgnoreCaseOrderByIdAsc(
                user.getId(),
                keyword));
        items.addAll(vocabSetRepository.findByUserIdAndVocabSetNameContainingIgnoreCaseOrderByIdAsc(
                user.getId(),
                keyword));

        return items.stream()
                .sorted(Comparator.comparing(Item::getId))
                .map(this::convertToDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public ResItemDTO getItemByPath(String path) {
        User user = currentUser();
        Item currentItem = null;

        for (String segment : pathSegments(path)) {
            if (currentItem instanceof VocabSet) {
                throw new IllegalArgumentException("Vocab set không chứa item con");
            }
            currentItem = resolvePathSegment(user.getId(), currentItem, segment);
        }

        return convertToDTO(currentItem);
    }

    private User currentUser() {
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy người dùng hiện tại"));
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy người dùng hiện tại"));
    }

    private String normalizeSearchName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Từ khóa tìm kiếm không được để trống");
        }
        return name.trim();
    }

    private List<String> pathSegments(String path) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("Path không được để trống");
        }

        String normalizedPath = path.trim();
        while (normalizedPath.startsWith("/")) {
            normalizedPath = normalizedPath.substring(1);
        }
        while (normalizedPath.endsWith("/")) {
            normalizedPath = normalizedPath.substring(0, normalizedPath.length() - 1);
        }
        if (normalizedPath.isBlank()) {
            throw new IllegalArgumentException("Path không được để trống");
        }

        return List.of(normalizedPath.split("/", -1)).stream()
                .map(String::trim)
                .peek(segment -> {
                    if (segment.isBlank()) {
                        throw new IllegalArgumentException("Path không được chứa segment trống");
                    }
                })
                .toList();
    }

    private Item resolvePathSegment(Long userId, Item parent, String segment) {
        List<Item> matches = matchingItems(userId, parent, segment);
        if (matches.isEmpty()) {
            throw new NoSuchElementException("Item path không tồn tại hoặc không thuộc người dùng hiện tại");
        }
        if (matches.size() > 1) {
            throw new IllegalArgumentException(
                    "Item path không đủ rõ do có nhiều item cùng tên trong cùng một thư mục");
        }
        return matches.get(0);
    }

    private List<Item> matchingItems(Long userId, Item parent, String segment) {
        List<Item> matches = new ArrayList<>();
        if (parent == null) {
            matches.addAll(folderRepository.findByUserIdAndParentIsNullAndFolderName(userId, segment));
            matches.addAll(vocabSetRepository.findByUserIdAndParentIsNullAndVocabSetName(userId, segment));
            return matches;
        }

        matches.addAll(folderRepository.findByUserIdAndParentIdAndFolderName(userId, parent.getId(), segment));
        matches.addAll(vocabSetRepository.findByUserIdAndParentIdAndVocabSetName(
                userId,
                parent.getId(),
                segment));
        return matches;
    }

    private ResItemDTO convertToDTO(Item item) {
        Long parentId = item.getParent() == null ? null : item.getParent().getId();
        if (item instanceof Folder folder) {
            return ResItemDTO.builder()
                    .id(folder.getId())
                    .type(ItemType.FOLDER)
                    .name(folder.getFolderName())
                    .parentId(parentId)
                    .itemPath(buildItemPath(folder))
                    .build();
        }
        VocabSet vocabSet = (VocabSet) item;
        return ResItemDTO.builder()
                .id(vocabSet.getId())
                .type(ItemType.VOCAB_SET)
                .name(vocabSet.getVocabSetName())
                .description(vocabSet.getVocabSetDescription())
                .parentId(parentId)
                .vocabCount(vocabSet.getVocabs().size())
                .itemPath(buildItemPath(vocabSet))
                .build();
    }

    private String buildItemPath(Item item) {
        List<String> segments = new ArrayList<>();
        Item currentItem = item;
        while (currentItem != null) {
            segments.add(displayName(currentItem));
            currentItem = currentItem.getParent();
        }

        Collections.reverse(segments);
        return "/" + String.join("/", segments);
    }

    private String displayName(Item item) {
        if (item instanceof Folder folder) {
            return folder.getFolderName();
        }
        VocabSet vocabSet = (VocabSet) item;
        return vocabSet.getVocabSetName();
    }
}
