package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.service.testing;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.responseDTO.ResOptionDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.responseDTO.ResQuestionDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.responseDTO.ResTestAnswerDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.responseDTO.ResTestDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.Option;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.Question;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.Test;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.TestAnswer;

@Component
public class TestResponseMapper {
    public ResTestDTO convertToDTO(Test test) {
        Map<Long, TestAnswer> answersByQuestionId = test.getAnswers().stream()
                .collect(Collectors.toMap(answer -> answer.getQuestion().getId(), Function.identity()));
        return ResTestDTO.builder()
                .id(test.getId())
                .numberOfQuestion(test.getNumberOfQuestion())
                .timeInMinute(test.getTimeInMinute())
                .correctAnswerCount(test.getCorrectAnswerCount())
                .incorrectAnswerCount(test.getIncorrectAnswerCount())
                .remainingTimeInSeconds(remainingTimeInSeconds(test))
                .finished(test.getFinishedAt() != null)
                .questions(test.getQuestions().stream()
                        .map(question -> convertToDTO(question, answersByQuestionId.get(question.getId())))
                        .toList())
                .build();
    }

    private ResQuestionDTO convertToDTO(Question question, TestAnswer answer) {
        return ResQuestionDTO.builder()
                .id(question.getId())
                .vocabId(question.getVocab().getId())
                .questionContent(question.getQuestionContent())
                .correctAnswer(question.getCorrectAnswer())
                .audioUrl(question.getAudioUrl())
                .options(question.getOptions().stream()
                        .map(this::convertToDTO)
                        .toList())
                .answer(convertAnswerToDTO(answer))
                .build();
    }

    private ResOptionDTO convertToDTO(Option option) {
        return ResOptionDTO.builder()
                .id(option.getId())
                .optionOrder(option.getOptionOrder())
                .optionContent(option.getOptionContent())
                .correct(option.isCorrect())
                .audioUrl(option.getAudioUrl())
                .build();
    }

    private ResTestAnswerDTO convertAnswerToDTO(TestAnswer answer) {
        if (answer == null) {
            return null;
        }
        Option selectedOption = answer.getSelectedOption();
        return ResTestAnswerDTO.builder()
                .id(answer.getId())
                .questionId(answer.getQuestion().getId())
                .selectedOptionId(selectedOption == null ? null : selectedOption.getId())
                .selectedOptionContent(selectedOption == null ? null : selectedOption.getOptionContent())
                .correct(answer.isCorrect())
                .build();
    }

    private Long remainingTimeInSeconds(Test test) {
        if (test.getFinishedAt() != null || test.getTimeInMinute() == null || test.getStartedAt() == null) {
            return null;
        }
        Instant deadline = test.getStartedAt().plus(Duration.ofMinutes(test.getTimeInMinute()));
        long remainingSeconds = Duration.between(Instant.now(), deadline).toSeconds();
        return Math.max(remainingSeconds, 0L);
    }
}
