package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.service.testing;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.requestDTO.ReqFinishTestDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.requestDTO.ReqTestAnswerDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.responseDTO.ResTestDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.Option;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.Question;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.Test;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.User;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.Vocab;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository.TestRepository;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository.UserRepository;

class TestResultServiceTests {
    private static final Long USER_ID = 1L;
    private static final String EMAIL = "learner@example.com";
    private static final Long TEST_ID = 99L;

    private final UserRepository userRepository = Mockito.mock(UserRepository.class);
    private final TestRepository testRepository = Mockito.mock(TestRepository.class);
    private final VocabSourceResolver vocabSourceResolver = Mockito.mock(VocabSourceResolver.class);
    private final QuestionFactory questionFactory = Mockito.mock(QuestionFactory.class);
    private final OptionGenerator optionGenerator = Mockito.mock(OptionGenerator.class);
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
    private final TestResultService testResultService = new TestResultService(
            testRepository,
            testService,
            testResponseMapper);

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @org.junit.jupiter.api.Test
    void shouldSaveFinalAnswersAndUpdateResultCounts() {
        Test test = testEntity(false);
        mockCurrentUser(test);
        Mockito.when(testRepository.save(Mockito.any())).thenAnswer(invocation -> invocation.getArgument(0));

        ResTestDTO result = testResultService.finish(TEST_ID, ReqFinishTestDTO.builder()
                .answers(List.of(ReqTestAnswerDTO.builder()
                        .questionId(10L)
                        .optionId(100L)
                        .build()))
                .build());

        Assertions.assertThat(result).satisfies(finishedTest -> {
            if (!finishedTest.isFinished()
                    || finishedTest.getCorrectAnswerCount() != 1
                    || finishedTest.getIncorrectAnswerCount() != 1
                    || finishedTest.getQuestions().size() != 2) {
                throw new AssertionError("Result count hoặc trạng thái finish không đúng");
            }
            if (finishedTest.getQuestions().get(0).getAnswer() == null
                    || !finishedTest.getQuestions().get(0).getAnswer().isCorrect()
                    || finishedTest.getQuestions().get(1).getAnswer() == null
                    || finishedTest.getQuestions().get(1).getAnswer().isCorrect()) {
                throw new AssertionError("Final answers phải được lưu cho cả câu trả lời và câu bỏ trống");
            }
        });
    }

    @org.junit.jupiter.api.Test
    void shouldRejectDuplicateQuestionAnswers() {
        Test test = testEntity(false);
        mockCurrentUser(test);

        Assertions.assertThatThrownBy(() -> testResultService.finish(TEST_ID, ReqFinishTestDTO.builder()
                        .answers(List.of(
                                ReqTestAnswerDTO.builder().questionId(10L).optionId(100L).build(),
                                ReqTestAnswerDTO.builder().questionId(10L).optionId(101L).build()))
                        .build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Không được gửi trùng câu trả lời cho cùng một câu hỏi");
    }

    @org.junit.jupiter.api.Test
    void shouldRejectExpiredTest() {
        Test test = testEntity(true);
        mockCurrentUser(test);

        Assertions.assertThatThrownBy(() -> testResultService.finish(TEST_ID, ReqFinishTestDTO.builder()
                        .answers(List.of())
                        .build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Bài test đã hết thời gian làm bài");
    }

    private void mockCurrentUser(Test test) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(EMAIL, "n/a"));
        Mockito.when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(currentUser));
        Mockito.when(testRepository.findByIdAndUserId(TEST_ID, USER_ID)).thenReturn(Optional.of(test));
    }

    private Test testEntity(boolean expired) {
        Test test = Test.builder()
                .id(TEST_ID)
                .user(currentUser)
                .numberOfQuestion(2)
                .timeInMinute(expired ? 1 : null)
                .startedAt(expired ? Instant.now().minusSeconds(120) : Instant.now())
                .build();
        Question firstQuestion = question(test, 10L, 100L, 101L);
        Question secondQuestion = question(test, 11L, 110L, 111L);
        test.getQuestions().addAll(List.of(firstQuestion, secondQuestion));
        return test;
    }

    private Question question(Test test, Long questionId, Long correctOptionId, Long incorrectOptionId) {
        Question question = Question.builder()
                .id(questionId)
                .test(test)
                .vocab(Vocab.builder().id(questionId).word("word-" + questionId).build())
                .questionContent("Question " + questionId)
                .correctAnswer("correct")
                .build();
        question.getOptions().addAll(List.of(
                option(question, correctOptionId, true),
                option(question, incorrectOptionId, false)));
        return question;
    }

    private Option option(Question question, Long optionId, boolean correct) {
        return Option.builder()
                .id(optionId)
                .question(question)
                .optionOrder(correct ? 1 : 2)
                .optionContent(correct ? "correct" : "incorrect")
                .correct(correct)
                .build();
    }
}
