package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.service.organization;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.requestDTO.ReqBulkAddVocabToSetDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.responseDTO.ResVocabDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.responseDTO.ResVocabSetBulkAddDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.responseDTO.ResVocabSetBulkAddItemDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.responseDTO.ResVocabSetSummaryDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.responseDTO.ResVocabSetVocabDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.User;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.Vocab;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.VocabSet;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository.UserRepository;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository.VocabRepository;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository.VocabSetRepository;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.util.SecurityUtil;

@Service
@RequiredArgsConstructor
public class VocabSetMembershipService {
    private final UserRepository userRepository;
    private final VocabRepository vocabRepository;
    private final VocabSetRepository vocabSetRepository;

    @Transactional
    public ResVocabSetVocabDTO addVocabToSet(Long vocabSetId, Long vocabId) {
        User user = currentUser();
        VocabSet vocabSet = resolveVocabSet(vocabSetId, user.getId());
        if (vocabId == null) {
            throw new IllegalArgumentException("Vocab id không được để trống");
        }
        Vocab vocab = vocabRepository.findById(vocabId)
                .orElseThrow(() -> new NoSuchElementException("Vocab không tồn tại"));
        boolean added = addIfMissing(vocabSet, vocab);
        if (added) {
            vocabSetRepository.save(vocabSet);
        }

        return ResVocabSetVocabDTO.builder()
                .vocabSet(convertToVocabSetSummary(vocabSet))
                .vocab(convertToVocabDTO(vocab))
                .added(added)
                .build();
    }

    @Transactional
    public ResVocabSetVocabDTO addVocabToSet(Long vocabSetId, Vocab vocab) {
        User user = currentUser();
        VocabSet vocabSet = resolveVocabSet(vocabSetId, user.getId());
        boolean added = addIfMissing(vocabSet, vocab);
        if (added) {
            vocabSetRepository.save(vocabSet);
        }

        return ResVocabSetVocabDTO.builder()
                .vocabSet(convertToVocabSetSummary(vocabSet))
                .vocab(convertToVocabDTO(vocab))
                .added(added)
                .build();
    }

    @Transactional(readOnly = true)
    public void validateVocabSetAccess(Long vocabSetId) {
        User user = currentUser();
        resolveVocabSet(vocabSetId, user.getId());
    }

    @Transactional
    public ResVocabSetBulkAddDTO bulkAddVocabsToSet(Long vocabSetId, ReqBulkAddVocabToSetDTO request) {
        if (request == null || request.getVocabIds() == null) {
            throw new IllegalArgumentException("Danh sách vocabIds không được để trống");
        }

        User user = currentUser();
        VocabSet vocabSet = resolveVocabSet(vocabSetId, user.getId());
        List<ResVocabSetBulkAddItemDTO> items = request.getVocabIds().stream()
                .map(vocabId -> addBulkItem(vocabSet, vocabId))
                .toList();
        vocabSetRepository.save(vocabSet);

        int successCount = (int) items.stream()
                .filter(item -> Boolean.TRUE.equals(item.getSuccess()))
                .count();
        return ResVocabSetBulkAddDTO.builder()
                .vocabSet(convertToVocabSetSummary(vocabSet))
                .total(items.size())
                .success(successCount)
                .failed(items.size() - successCount)
                .items(items)
                .build();
    }

    private ResVocabSetBulkAddItemDTO addBulkItem(VocabSet vocabSet, Long vocabId) {
        if (vocabId == null) {
            return ResVocabSetBulkAddItemDTO.builder()
                    .vocabId(null)
                    .success(false)
                    .added(false)
                    .error("Vocab id không được để trống")
                    .build();
        }

        return vocabRepository.findById(vocabId)
                .map(vocab -> {
                    boolean added = addIfMissing(vocabSet, vocab);
                    return ResVocabSetBulkAddItemDTO.builder()
                            .vocabId(vocabId)
                            .success(true)
                            .added(added)
                            .vocab(convertToVocabDTO(vocab))
                            .build();
                })
                .orElseGet(() -> ResVocabSetBulkAddItemDTO.builder()
                        .vocabId(vocabId)
                        .success(false)
                        .added(false)
                        .error("Vocab không tồn tại")
                        .build());
    }

    private boolean addIfMissing(VocabSet vocabSet, Vocab vocab) {
        boolean exists = vocabSet.getVocabs().stream()
                .anyMatch(existingVocab -> Objects.equals(existingVocab.getId(), vocab.getId()));
        if (exists) {
            return false;
        }

        vocabSet.getVocabs().add(vocab);
        return true;
    }

    private User currentUser() {
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy người dùng hiện tại"));
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy người dùng hiện tại"));
    }

    private VocabSet resolveVocabSet(Long vocabSetId, Long userId) {
        if (vocabSetId == null) {
            throw new IllegalArgumentException("Vocab set id không được để trống");
        }
        return vocabSetRepository.findByIdAndUserId(vocabSetId, userId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Vocab set không tồn tại hoặc không thuộc người dùng hiện tại"));
    }

    private ResVocabSetSummaryDTO convertToVocabSetSummary(VocabSet vocabSet) {
        Long parentId = vocabSet.getParent() == null ? null : vocabSet.getParent().getId();
        return ResVocabSetSummaryDTO.builder()
                .id(vocabSet.getId())
                .name(vocabSet.getVocabSetName())
                .description(vocabSet.getVocabSetDescription())
                .parentId(parentId)
                .vocabCount(vocabSet.getVocabs().size())
                .build();
    }

    private ResVocabDTO convertToVocabDTO(Vocab vocab) {
        return ResVocabDTO.builder()
                .id(vocab.getId())
                .word(vocab.getWord())
                .meaning(vocab.getMeaning())
                .ipa(vocab.getIpa())
                .audioUrl(vocab.getAudioUrl())
                .build();
    }
}
