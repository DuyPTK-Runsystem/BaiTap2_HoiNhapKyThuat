package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.service.vocab;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.requestDTO.ReqCreateVocabDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository.VocabRepository;

class VocabCreateValidationServiceTests {
    private static final String HELLO = "hello";
    private static final String IPA = "həˈləʊ";
    private static final String MEANING = "xin chao";

    private final VocabRepository vocabRepository = Mockito.mock(VocabRepository.class);

    @Test
    void createShouldRejectBlankWord() {
        VocabService vocabService = vocabService(Mockito.mock(VocabAutomationService.class));

        Assertions.assertThatThrownBy(() -> vocabService.create(ReqCreateVocabDTO.builder().word(" ").build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Word không được để trống");
    }

    @Test
    void createShouldRejectMissingMeaningWhenIpaCannotBeResolved() {
        VocabAutomationService automationService = Mockito.mock(VocabAutomationService.class);
        Mockito.when(automationService.resolve(HELLO)).thenReturn(Optional.empty());
        VocabService vocabService = vocabService(automationService);

        Assertions.assertThatThrownBy(() -> vocabService.create(ReqCreateVocabDTO.builder().word(HELLO).build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Nếu không tìm thấy IPA, Word, Meaning và IPA là bắt buộc");
    }

    @Test
    void createShouldRejectExistingWord() {
        Mockito.when(vocabRepository.existsByWord(HELLO)).thenReturn(true);
        VocabService vocabService = vocabService(Mockito.mock(VocabAutomationService.class));

        Assertions.assertThatThrownBy(() -> vocabService.create(createRequest()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Từ vựng đã tồn tại");
    }

    private VocabService vocabService(VocabAutomationService automationService) {
        VocabValidationService validationService = new VocabValidationService(vocabRepository);
        VocabLookupService lookupService = new VocabLookupService(vocabRepository, validationService);
        return new VocabService(
                vocabRepository,
                automationService,
                Mockito.mock(VocabAudioService.class),
                lookupService,
                validationService);
    }

    private ReqCreateVocabDTO createRequest() {
        return ReqCreateVocabDTO.builder()
                .word(HELLO)
                .meaning(MEANING)
                .ipa(IPA)
                .build();
    }
}
