package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.service.vocab;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.externalDTO.VocabAutomationResult;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.requestDTO.ReqCreateVocabDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.responseDTO.ResVocabDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.Vocab;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository.VocabRepository;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.service.organization.VocabSetMembershipService;

class VocabServiceTests {
        private static final String AUDIO_URL = "https://example.com/hello.mp3";
        private static final String HELLO = "hello";
        private static final String IPA = "həˈləʊ";
        private static final String MEANING = "xin chao";

        private final VocabRepository vocabRepository = Mockito.mock(VocabRepository.class);

        @Test
        void createShouldKeepProvidedIpaAndSynthesizeAudio() {
                VocabAutomationService automationService = Mockito.mock(VocabAutomationService.class);
                VocabAudioService vocabAudioService = Mockito.mock(VocabAudioService.class);
                Mockito.when(vocabAudioService.synthesizeAndStoreAudio(HELLO, IPA)).thenReturn(AUDIO_URL);
                VocabService vocabService = vocabService(automationService, vocabAudioService);
                mockSave();

                ResVocabDTO response = vocabService.create(createRequest(IPA));

                Assertions.assertThat(response)
                                .extracting(ResVocabDTO::getIpa, ResVocabDTO::getAudioUrl, ResVocabDTO::isMastered)
                                .containsExactly(IPA, AUDIO_URL, false);
        }

        @Test
        void createShouldNotCallAutomationWhenIpaIsProvided() {
                VocabAutomationService automationService = Mockito.mock(VocabAutomationService.class);
                VocabAudioService vocabAudioService = Mockito.mock(VocabAudioService.class);
                Mockito.when(vocabAudioService.synthesizeAndStoreAudio(HELLO, IPA)).thenReturn(AUDIO_URL);
                VocabService vocabService = vocabService(automationService, vocabAudioService);
                mockSave();

                vocabService.create(createRequest(IPA));

                Mockito.verifyNoInteractions(automationService);
        }

        @Test
        void createShouldResolveAutomationValuesWhenMissing() {
                VocabAutomationService automationService = Mockito.mock(VocabAutomationService.class);
                Mockito.when(automationService.resolve(HELLO))
                                .thenReturn(Optional.of(automationResult(IPA, AUDIO_URL)));
                VocabAudioService vocabAudioService = Mockito.mock(VocabAudioService.class);
                VocabService vocabService = vocabService(automationService, vocabAudioService);
                mockSave();

                ResVocabDTO response = vocabService.create(createRequest(null));

                Assertions.assertThat(response)
                                .extracting(
                                                ResVocabDTO::getIpa,
                                                ResVocabDTO::getAudioUrl)
                                .containsExactly(IPA, AUDIO_URL);
        }

        @Test
        void createShouldSynthesizeAudioWhenAudioUrlIsMissing() {
                VocabAutomationService automationService = Mockito.mock(VocabAutomationService.class);
                Mockito.when(automationService.resolve(HELLO))
                                .thenReturn(Optional.of(automationResult(IPA, null)));
                VocabAudioService vocabAudioService = Mockito.mock(VocabAudioService.class);
                Mockito.when(vocabAudioService.synthesizeAndStoreAudio(HELLO, IPA)).thenReturn(AUDIO_URL);
                VocabService vocabService = vocabService(automationService, vocabAudioService);
                mockSave();

                ResVocabDTO response = vocabService.create(createRequest(null));

                Assertions.assertThat(response.getAudioUrl()).isEqualTo(AUDIO_URL);
        }

        private void mockSave() {
                Mockito.when(vocabRepository.save(ArgumentMatchers.any(Vocab.class)))
                                .thenAnswer(invocation -> {
                                        Vocab vocab = invocation.getArgument(0);
                                        vocab.setId(1L);
                                        return vocab;
                                });
        }

        private VocabAutomationResult automationResult(String ipa, String audioUrl) {
                return VocabAutomationResult.builder()
                                .ipa(ipa)
                                .audioUrl(audioUrl)
                                .build();
        }

        private ReqCreateVocabDTO createRequest(String ipa) {
                return ReqCreateVocabDTO.builder()
                                .word(HELLO)
                                .meaning(MEANING)
                                .ipa(ipa)
                                .build();
        }

        private VocabService vocabService(VocabAutomationService automationService, VocabAudioService audioService) {
                VocabValidationService validationService = new VocabValidationService(vocabRepository);
                VocabLookupService lookupService = new VocabLookupService(vocabRepository, validationService);
                return new VocabService(
                                vocabRepository,
                                automationService,
                                audioService,
                                lookupService,
                                validationService,
                                Mockito.mock(VocabSetMembershipService.class));
        }
}
