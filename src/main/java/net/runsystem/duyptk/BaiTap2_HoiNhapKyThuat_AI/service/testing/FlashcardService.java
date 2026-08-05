package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.service.testing;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.requestDTO.ReqCreateFlashcardDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.responseDTO.ResFlashcardDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.responseDTO.ResFlashcardSessionDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.Vocab;

@Service
@RequiredArgsConstructor
public class FlashcardService {
    private final VocabSourceResolver vocabSourceResolver;

    @Transactional(readOnly = true)
    public ResFlashcardSessionDTO create(ReqCreateFlashcardDTO request) {
        validateRequest(request);
        List<ResFlashcardDTO> flashcards = vocabSourceResolver.resolveAll(request.getSourceItemIds()).stream()
                .filter(this::canCreateFlashcard)
                .limit(request.getNumberOfFlashcards())
                .map(this::convertToDTO)
                .toList();
        if (flashcards.size() < request.getNumberOfFlashcards()) {
            throw new IllegalArgumentException("Không đủ từ vựng hợp lệ để tạo flashcard");
        }

        return ResFlashcardSessionDTO.builder()
                .sourceItemIds(request.getSourceItemIds())
                .numberOfFlashcards(flashcards.size())
                .flashcards(flashcards)
                .build();
    }

    private void validateRequest(ReqCreateFlashcardDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Request tạo flashcard không được để trống");
        }
        if (request.getNumberOfFlashcards() == null || request.getNumberOfFlashcards() < 1) {
            throw new IllegalArgumentException("Số lượng flashcard phải lớn hơn hoặc bằng 1");
        }
    }

    private boolean canCreateFlashcard(Vocab vocab) {
        return hasText(vocab.getMeaning()) || hasText(vocab.getAudioUrl());
    }

    private ResFlashcardDTO convertToDTO(Vocab vocab) {
        FlashcardFrontType frontType = randomFrontType(vocab);
        return ResFlashcardDTO.builder()
                .vocabId(vocab.getId())
                .frontType(frontType)
                .frontText(frontType == FlashcardFrontType.MEANING ? vocab.getMeaning() : null)
                .frontAudioUrl(frontType == FlashcardFrontType.AUDIO ? vocab.getAudioUrl() : null)
                .backWord(vocab.getWord())
                .backMeaning(vocab.getMeaning())
                .backAudioUrl(vocab.getAudioUrl())
                .build();
    }

    private FlashcardFrontType randomFrontType(Vocab vocab) {
        List<FlashcardFrontType> availableTypes = new ArrayList<>();
        if (hasText(vocab.getMeaning())) {
            availableTypes.add(FlashcardFrontType.MEANING);
        }
        if (hasText(vocab.getAudioUrl())) {
            availableTypes.add(FlashcardFrontType.AUDIO);
        }
        return availableTypes.get(ThreadLocalRandom.current().nextInt(availableTypes.size()));
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
