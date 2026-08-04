package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.service.vocab;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.requestDTO.ReqUpdateVocabDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.responseDTO.ResVocabDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.Vocab;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository.VocabRepository;

class VocabLookupUpdateServiceTests {
    private static final String AUDIO_URL = "https://example.com/hello.mp3";
    private static final String HELLO = "hello";
    private static final String IPA = "həˈləʊ";
    private static final String MEANING = "xin chao";
    private static final String UPDATED_MEANING = "xin chao moi";

    private final VocabRepository vocabRepository = Mockito.mock(VocabRepository.class);

    @Test
    void lookupShouldFindByWord() {
        Mockito.when(vocabRepository.findByWord(HELLO)).thenReturn(Optional.of(vocab()));
        VocabService vocabService = vocabService();

        ResVocabDTO response = vocabService.get(null, HELLO);

        Assertions.assertThat(response)
                .extracting(ResVocabDTO::getWord, ResVocabDTO::getMeaning)
                .containsExactly(HELLO, MEANING);
    }

    @Test
    void updateShouldOnlyChangeMeaning() {
        Mockito.when(vocabRepository.findById(1L)).thenReturn(Optional.of(vocab()));
        Mockito.when(vocabRepository.save(ArgumentMatchers.any(Vocab.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        VocabService vocabService = vocabService();

        ResVocabDTO response = vocabService.update(
                1L,
                null,
                ReqUpdateVocabDTO.builder().meaning(UPDATED_MEANING).build());

        Assertions.assertThat(response)
                .extracting(ResVocabDTO::getWord, ResVocabDTO::getMeaning)
                .containsExactly(HELLO, UPDATED_MEANING);
    }

    private VocabService vocabService() {
        VocabValidationService validationService = new VocabValidationService(vocabRepository);
        VocabLookupService lookupService = new VocabLookupService(vocabRepository, validationService);
        return new VocabService(
                vocabRepository,
                Mockito.mock(VocabAutomationService.class),
                Mockito.mock(VocabAudioService.class),
                lookupService,
                validationService);
    }

    private Vocab vocab() {
        return Vocab.builder()
                .id(1L)
                .word(HELLO)
                .meaning(MEANING)
                .ipa(IPA)
                .audioUrl(AUDIO_URL)
                .build();
    }
}
