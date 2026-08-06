package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.service.testing;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.requestDTO.ReqFinishTestDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.requestDTO.ReqTestAnswerDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.responseDTO.ResTestDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.Option;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.Question;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.Test;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.TestAnswer;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository.TestRepository;

@Service
@RequiredArgsConstructor
public class TestResultService {
    private final TestRepository testRepository;
    private final TestService testService;
    private final TestResponseMapper testResponseMapper;

    @Transactional
    public ResTestDTO finish(Long testId, ReqFinishTestDTO request) {
        validateRequest(request);
        Test test = testService.findOwnedTest(testId);
        validateCanFinish(test);
        Map<Long, Long> selectedOptionIdsByQuestionId = selectedOptionIdsByQuestionId(request.getAnswers());
        Map<Long, Question> questionsById = questionsById(test);
        validateQuestionIds(selectedOptionIdsByQuestionId.keySet(), questionsById);

        test.getAnswers().clear();
        int correctAnswerCount = 0;
        for (Question question : test.getQuestions()) {
            Option selectedOption = selectedOption(question, selectedOptionIdsByQuestionId.get(question.getId()));
            boolean correct = selectedOption != null && selectedOption.isCorrect();
            if (correct) {
                question.getVocab().setMastered(true);
                correctAnswerCount++;
            }
            test.getAnswers().add(TestAnswer.builder()
                    .test(test)
                    .question(question)
                    .selectedOption(selectedOption)
                    .correct(correct)
                    .build());
        }

        test.setCorrectAnswerCount(correctAnswerCount);
        test.setIncorrectAnswerCount(test.getNumberOfQuestion() - correctAnswerCount);
        test.setFinishedAt(Instant.now());
        return testResponseMapper.convertToDTO(testRepository.save(test));
    }

    private void validateRequest(ReqFinishTestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Request kết thúc bài test không được để trống");
        }
        if (request.getAnswers() == null) {
            throw new IllegalArgumentException("Danh sách đáp án không được để trống");
        }
    }

    private void validateCanFinish(Test test) {
        if (test.getFinishedAt() != null) {
            throw new IllegalArgumentException("Bài test đã kết thúc");
        }
        if (isExpired(test)) {
            throw new IllegalArgumentException("Bài test đã hết thời gian làm bài");
        }
    }

    private boolean isExpired(Test test) {
        if (test.getTimeInMinute() == null || test.getStartedAt() == null) {
            return false;
        }
        Instant deadline = test.getStartedAt().plus(Duration.ofMinutes(test.getTimeInMinute()));
        return !Instant.now().isBefore(deadline);
    }

    private Map<Long, Long> selectedOptionIdsByQuestionId(List<ReqTestAnswerDTO> answers) {
        Map<Long, Long> selectedOptionIdsByQuestionId = new HashMap<>();
        Set<Long> duplicatedQuestionIds = new HashSet<>();
        for (ReqTestAnswerDTO answer : answers) {
            validateAnswer(answer);
            if (selectedOptionIdsByQuestionId.containsKey(answer.getQuestionId())) {
                duplicatedQuestionIds.add(answer.getQuestionId());
            } else {
                selectedOptionIdsByQuestionId.put(answer.getQuestionId(), answer.getOptionId());
            }
        }
        if (!duplicatedQuestionIds.isEmpty()) {
            throw new IllegalArgumentException("Không được gửi trùng câu trả lời cho cùng một câu hỏi");
        }
        return selectedOptionIdsByQuestionId;
    }

    private void validateAnswer(ReqTestAnswerDTO answer) {
        if (answer == null || answer.getQuestionId() == null || answer.getQuestionId() < 1) {
            throw new IllegalArgumentException("Question id phải lớn hơn 0");
        }
        if (answer.getOptionId() != null && answer.getOptionId() < 1) {
            throw new IllegalArgumentException("Option id phải lớn hơn 0");
        }
    }

    private Map<Long, Question> questionsById(Test test) {
        return test.getQuestions().stream()
                .collect(Collectors.toMap(Question::getId, Function.identity()));
    }

    private void validateQuestionIds(Set<Long> questionIds, Map<Long, Question> questionsById) {
        if (!questionsById.keySet().containsAll(questionIds)) {
            throw new IllegalArgumentException("Question id không thuộc bài test hiện tại");
        }
    }

    private Option selectedOption(Question question, Long optionId) {
        if (optionId == null) {
            return null;
        }
        return question.getOptions().stream()
                .filter(option -> option.getId().equals(optionId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Option id không thuộc question hiện tại"));
    }
}
