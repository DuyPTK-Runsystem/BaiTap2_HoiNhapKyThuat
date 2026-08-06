package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.service.vocab;

import java.util.ArrayList;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.requestDTO.ReqCreateVocabDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.responseDTO.ResVocabDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.responseDTO.ResVocabSetSummaryDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.responseDTO.ResVocabSetVocabDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.Vocab;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository.VocabRepository;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.service.organization.VocabSetMembershipService;

class VocabCreateWithSetServiceTests {
    private static final Long VOCAB_SET_ID = 12L;
    private static final String AUDIO_URL = "https://example.com/go.mp3";
    private static final String IPA = "gəʊ";
    private static final String MEANING = "di chuyen";
    private static final String WORD = "go";

    private final VocabRepository vocabRepository = Mockito.mock(VocabRepository.class);
    private final VocabSetMembershipService membershipService = Mockito.mock(VocabSetMembershipService.class);

    @Test
    void createShouldReturnMembershipResponseWhenVocabSetIdIsProvided() {
        VocabService vocabService = vocabService();
        List<String> events = new ArrayList<>();
        Mockito.doAnswer(invocation -> {
            events.add("validated");
            return null;
        }).when(membershipService).validateVocabSetAccess(VOCAB_SET_ID);
        Mockito.when(vocabRepository.save(ArgumentMatchers.any(Vocab.class)))
                .thenAnswer(invocation -> withId(invocation.getArgument(0)));
        Mockito.when(membershipService.addVocabToSet(ArgumentMatchers.eq(VOCAB_SET_ID),
                ArgumentMatchers.any(Vocab.class))).thenAnswer(invocation -> {
                    events.add("added:" + invocation.<Vocab>getArgument(1).getWord());
                    return membershipResponse();
                });

        ResVocabSetVocabDTO response = vocabService.create(createRequest(), VOCAB_SET_ID);

        Assertions.assertThat(response)
                .extracting(
                        ResVocabSetVocabDTO::getAdded,
                        result -> result.getVocabSet().getId(),
                        result -> result.getVocab().getWord(),
                        result -> events)
                .containsExactly(true, VOCAB_SET_ID, WORD, List.of("validated", "added:" + WORD));
    }

    private VocabService vocabService() {
        VocabValidationService validationService = new VocabValidationService(vocabRepository);
        VocabLookupService lookupService = new VocabLookupService(vocabRepository, validationService);
        VocabAutomationService automationService = Mockito.mock(VocabAutomationService.class);
        VocabAudioService audioService = Mockito.mock(VocabAudioService.class);
        Mockito.when(audioService.synthesizeAndStoreAudio(WORD, IPA)).thenReturn(AUDIO_URL);
        return new VocabService(
                vocabRepository,
                automationService,
                audioService,
                lookupService,
                validationService,
                membershipService);
    }

    private ReqCreateVocabDTO createRequest() {
        return ReqCreateVocabDTO.builder()
                .word(WORD)
                .meaning(MEANING)
                .ipa(IPA)
                .build();
    }

    private Vocab withId(Vocab vocab) {
        vocab.setId(1L);
        return vocab;
    }

    private ResVocabSetVocabDTO membershipResponse() {
        return ResVocabSetVocabDTO.builder()
                .vocabSet(ResVocabSetSummaryDTO.builder()
                        .id(VOCAB_SET_ID)
                        .name("Common verbs")
                        .vocabCount(1)
                        .build())
                .vocab(ResVocabDTO.builder()
                        .id(1L)
                        .word(WORD)
                        .meaning(MEANING)
                        .ipa(IPA)
                        .audioUrl(AUDIO_URL)
                        .build())
                .added(true)
                .build();
    }
}
