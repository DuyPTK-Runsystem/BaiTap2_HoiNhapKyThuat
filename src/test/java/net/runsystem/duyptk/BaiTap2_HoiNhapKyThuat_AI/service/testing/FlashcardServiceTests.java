package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.service.testing;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.requestDTO.ReqCreateFlashcardDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.responseDTO.ResFlashcardSessionDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.Vocab;

class FlashcardServiceTests {
    private final VocabSourceResolver vocabSourceResolver = Mockito.mock(VocabSourceResolver.class);
    private final FlashcardService flashcardService = new FlashcardService(vocabSourceResolver);

    @Test
    void shouldCreateFlashcardsWithAvailableFrontTypes() {
        ReqCreateFlashcardDTO request = ReqCreateFlashcardDTO.builder()
                .sourceItemIds(List.of(10L))
                .numberOfFlashcards(2)
                .build();
        Mockito.when(vocabSourceResolver.resolveAll(List.of(10L)))
                .thenReturn(List.of(
                        vocab(1L, "apple", "quả táo", null),
                        vocab(2L, "book", null, "book.mp3")));

        ResFlashcardSessionDTO result = flashcardService.create(request);

        Assertions.assertThat(result).satisfies(session -> {
            if (session.getFlashcards().size() != 2 || session.getNumberOfFlashcards() != 2) {
                throw new AssertionError("Flashcard session phải trả đúng số lượng request");
            }
            if (session.getFlashcards().get(0).getFrontType() != FlashcardFrontType.MEANING
                    || session.getFlashcards().get(1).getFrontType() != FlashcardFrontType.AUDIO) {
                throw new AssertionError("Front type phải fallback theo dữ liệu hợp lệ của vocab");
            }
        });
    }

    @Test
    void shouldRejectInvalidFlashcardCount() {
        Assertions.assertThatThrownBy(() -> flashcardService.create(ReqCreateFlashcardDTO.builder()
                        .numberOfFlashcards(0)
                        .build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Số lượng flashcard phải lớn hơn hoặc bằng 1");
    }

    @Test
    void shouldRejectWhenValidVocabsAreInsufficient() {
        Mockito.when(vocabSourceResolver.resolveAll(null))
                .thenReturn(List.of(vocab(1L, "apple", null, null)));

        Assertions.assertThatThrownBy(() -> flashcardService.create(ReqCreateFlashcardDTO.builder()
                        .numberOfFlashcards(1)
                        .build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Không đủ từ vựng hợp lệ để tạo flashcard");
    }

    @Test
    void shouldSkipInvalidVocabsBeforeLimitingFlashcards() {
        Mockito.when(vocabSourceResolver.resolveAll(null))
                .thenReturn(List.of(
                        vocab(1L, "invalid", null, null),
                        vocab(2L, "apple", "quả táo", null),
                        vocab(3L, "book", null, "book.mp3")));

        ResFlashcardSessionDTO result = flashcardService.create(ReqCreateFlashcardDTO.builder()
                .numberOfFlashcards(2)
                .build());

        Assertions.assertThat(result.getFlashcards()).hasSize(2);
    }

    private Vocab vocab(Long id, String word, String meaning, String audioUrl) {
        return Vocab.builder()
                .id(id)
                .word(word)
                .meaning(meaning)
                .audioUrl(audioUrl)
                .build();
    }
}
