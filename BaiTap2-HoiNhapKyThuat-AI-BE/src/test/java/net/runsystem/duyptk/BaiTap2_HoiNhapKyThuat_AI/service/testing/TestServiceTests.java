package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.service.testing;

import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.requestDTO.ReqCreateTestDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.responseDTO.ResTestDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.User;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.Vocab;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository.TestRepository;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository.UserRepository;

class TestServiceTests {
    private static final Long USER_ID = 1L;
    private static final String EMAIL = "learner@example.com";

    private final UserRepository userRepository = Mockito.mock(UserRepository.class);
    private final TestRepository testRepository = Mockito.mock(TestRepository.class);
    private final VocabSourceResolver vocabSourceResolver = Mockito.mock(VocabSourceResolver.class);
    private final QuestionFactory questionFactory = new QuestionFactory();
    private final OptionGenerator optionGenerator = new OptionGenerator();
    private final TestResponseMapper testResponseMapper = new TestResponseMapper();
    private final User currentUser = User.builder()
            .id(USER_ID)
            .email(EMAIL)
            .password("hashed")
            .build();
    private final TestService testService = new TestService(
            userRepository,
            testRepository,
            vocabSourceResolver,
            questionFactory,
            optionGenerator,
            testResponseMapper);

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldCreateTestWithGeneratedQuestionsAndOptions() {
        mockCurrentUser();
        Mockito.when(vocabSourceResolver.resolve(List.of(10L), 4)).thenReturn(vocabs());
        Mockito.when(testRepository.save(Mockito.any())).thenAnswer(invocation -> invocation.getArgument(0));
        ReqCreateTestDTO request = ReqCreateTestDTO.builder()
                .sourceItemIds(List.of(10L))
                .numberOfQuestion(2)
                .timeInMinute(15)
                .build();

        ResTestDTO result = testService.create(request);

        Assertions.assertThat(result).satisfies(createdTest -> {
            if (!Integer.valueOf(2).equals(createdTest.getNumberOfQuestion())
                    || !Integer.valueOf(15).equals(createdTest.getTimeInMinute())
                    || createdTest.getCorrectAnswerCount() != 0
                    || createdTest.getIncorrectAnswerCount() != 0
                    || createdTest.getQuestions().size() != 2) {
                throw new AssertionError("Response tạo test không đúng metadata mong đợi");
            }
            createdTest.getQuestions().forEach(question -> {
                if (question.getOptions().size() != 4
                        || question.getOptions().stream().filter(option -> option.isCorrect()).count() != 1) {
                    throw new AssertionError("Mỗi question phải có 4 option và một đáp án đúng");
                }
            });
        });
    }

    @Test
    void shouldNormalizeZeroTimeLimitToNull() {
        mockCurrentUser();
        Mockito.when(vocabSourceResolver.resolve(null, 4)).thenReturn(vocabs());
        Mockito.when(testRepository.save(Mockito.any())).thenAnswer(invocation -> invocation.getArgument(0));

        ResTestDTO result = testService.create(ReqCreateTestDTO.builder()
                .numberOfQuestion(1)
                .timeInMinute(0)
                .build());

        Assertions.assertThat(result.getTimeInMinute()).isNull();
    }

    @Test
    void shouldUseQuestionTypeWithEnoughDistractors() {
        mockCurrentUser();
        Mockito.when(vocabSourceResolver.resolve(List.of(10L), 4)).thenReturn(vocabsWithoutAudioDistractors());
        Mockito.when(testRepository.save(Mockito.any())).thenAnswer(invocation -> invocation.getArgument(0));

        ResTestDTO result = testService.create(ReqCreateTestDTO.builder()
                .sourceItemIds(List.of(10L))
                .numberOfQuestion(1)
                .build());

        Assertions.assertThat(result.getQuestions().get(0).getOptions()).hasSize(4);
    }

    @Test
    void shouldRejectInvalidRequest() {
        Assertions.assertThatThrownBy(() -> testService.create(ReqCreateTestDTO.builder()
                        .numberOfQuestion(0)
                        .build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Số lượng câu hỏi phải lớn hơn hoặc bằng 1");
    }

    private void mockCurrentUser() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(EMAIL, "n/a"));
        Mockito.when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(currentUser));
    }

    private List<Vocab> vocabs() {
        return List.of(
                vocab(1L, "apple", "quả táo"),
                vocab(2L, "book", "quyển sách"),
                vocab(3L, "cat", "con mèo"),
                vocab(4L, "dog", "con chó"));
    }

    private List<Vocab> vocabsWithoutAudioDistractors() {
        return List.of(
                vocab(1L, "apple", "quả táo", "apple.mp3"),
                vocab(2L, "book", "quyển sách", null),
                vocab(3L, "cat", "con mèo", null),
                vocab(4L, "dog", "con chó", null));
    }

    private Vocab vocab(Long id, String word, String meaning) {
        return vocab(id, word, meaning, word + ".mp3");
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
