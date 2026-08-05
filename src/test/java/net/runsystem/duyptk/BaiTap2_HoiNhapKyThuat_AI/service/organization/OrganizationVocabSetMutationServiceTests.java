package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.service.organization;

import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.requestDTO.ReqBulkAddVocabToSetDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.responseDTO.ResVocabSetBulkAddDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.responseDTO.ResVocabSetVocabDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.User;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.Vocab;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.VocabSet;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository.UserRepository;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository.VocabRepository;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository.VocabSetRepository;

class OrganizationVocabSetMutationServiceTests {
    private static final Long EXISTING_VOCAB_ID = 6L;
    private static final Long MISSING_VOCAB_ID = 999L;
    private static final Long USER_ID = 1L;
    private static final Long VOCAB_ID = 5L;
    private static final Long VOCAB_SET_ID = 12L;
    private static final String EMAIL = "learner@example.com";

    private final UserRepository userRepository = Mockito.mock(UserRepository.class);
    private final VocabRepository vocabRepository = Mockito.mock(VocabRepository.class);
    private final VocabSetRepository vocabSetRepository = Mockito.mock(VocabSetRepository.class);
    private final VocabSetMembershipService vocabSetMembershipService = new VocabSetMembershipService(
            userRepository,
            vocabRepository,
            vocabSetRepository);

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAddOneVocabAndReturnVocabSetAndVocabInformation() {
        mockCurrentUser();
        VocabSet vocabSet = vocabSet();
        Vocab vocab = vocab(VOCAB_ID, "go");
        Mockito.when(vocabSetRepository.findByIdAndUserId(VOCAB_SET_ID, USER_ID)).thenReturn(Optional.of(vocabSet));
        Mockito.when(vocabRepository.findById(VOCAB_ID)).thenReturn(Optional.of(vocab));

        ResVocabSetVocabDTO response = vocabSetMembershipService.addVocabToSet(VOCAB_SET_ID, VOCAB_ID);

        Assertions.assertThat(List.of(
                response.getAdded(),
                response.getVocabSet().getId(),
                response.getVocabSet().getName(),
                response.getVocabSet().getVocabCount(),
                response.getVocab().getId(),
                response.getVocab().getWord()))
                .containsExactly(true, VOCAB_SET_ID, "Common verbs", 1, VOCAB_ID, "go");
    }

    @Test
    void shouldTreatExistingVocabSetRelationAsIdempotentSuccess() {
        mockCurrentUser();
        Vocab vocab = vocab(VOCAB_ID, "go");
        VocabSet vocabSet = vocabSet();
        vocabSet.getVocabs().add(vocab);
        Mockito.when(vocabSetRepository.findByIdAndUserId(VOCAB_SET_ID, USER_ID)).thenReturn(Optional.of(vocabSet));
        Mockito.when(vocabRepository.findById(VOCAB_ID)).thenReturn(Optional.of(vocab));

        ResVocabSetVocabDTO response = vocabSetMembershipService.addVocabToSet(VOCAB_SET_ID, VOCAB_ID);

        Assertions.assertThat(List.of(response.getAdded(), response.getVocabSet().getVocabCount()))
                .containsExactly(false, 1);
    }

    @Test
    void shouldBulkAddWithPartialFailure() {
        mockCurrentUser();
        VocabSet vocabSet = vocabSet();
        vocabSet.getVocabs().add(vocab(EXISTING_VOCAB_ID, "run"));
        Mockito.when(vocabSetRepository.findByIdAndUserId(VOCAB_SET_ID, USER_ID)).thenReturn(Optional.of(vocabSet));
        Mockito.when(vocabRepository.findById(VOCAB_ID)).thenReturn(Optional.of(vocab(VOCAB_ID, "go")));
        Mockito.when(vocabRepository.findById(EXISTING_VOCAB_ID))
                .thenReturn(Optional.of(vocab(EXISTING_VOCAB_ID, "run")));
        Mockito.when(vocabRepository.findById(MISSING_VOCAB_ID)).thenReturn(Optional.empty());

        ResVocabSetBulkAddDTO response = vocabSetMembershipService.bulkAddVocabsToSet(
                VOCAB_SET_ID,
                ReqBulkAddVocabToSetDTO.builder()
                        .vocabIds(List.of(VOCAB_ID, EXISTING_VOCAB_ID, MISSING_VOCAB_ID))
                        .build());

        Assertions.assertThat(List.of(
                response.getTotal(),
                response.getSuccess(),
                response.getFailed(),
                response.getItems().get(0).getVocabId(),
                response.getItems().get(0).getSuccess(),
                response.getItems().get(0).getAdded(),
                response.getItems().get(1).getVocabId(),
                response.getItems().get(1).getSuccess(),
                response.getItems().get(1).getAdded(),
                response.getItems().get(2).getVocabId(),
                response.getItems().get(2).getSuccess(),
                response.getItems().get(2).getAdded()))
                .containsExactly(3, 2, 1, VOCAB_ID, true, true, EXISTING_VOCAB_ID, true, false,
                        MISSING_VOCAB_ID, false, false);
    }

    private void mockCurrentUser() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(EMAIL, "n/a"));
        Mockito.when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(currentUser()));
    }

    private User currentUser() {
        return User.builder()
                .id(USER_ID)
                .email(EMAIL)
                .password("hashed")
                .build();
    }

    private VocabSet vocabSet() {
        return VocabSet.builder()
                .id(VOCAB_SET_ID)
                .vocabSetName("Common verbs")
                .user(currentUser())
                .build();
    }

    private Vocab vocab(Long id, String word) {
        return Vocab.builder()
                .id(id)
                .word(word)
                .meaning("meaning")
                .ipa("ipa")
                .audioUrl("/api/v1/vocabs/audio/" + word + ".mp3")
                .build();
    }
}
