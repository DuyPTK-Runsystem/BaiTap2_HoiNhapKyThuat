package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.service.testing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Component;

import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.Question;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.Vocab;

@Component
public class QuestionFactory {
    public QuestionResult create(Vocab vocab) {
        List<QuestionType> supportedTypes = supportedTypes(vocab);
        if (supportedTypes.isEmpty()) {
            throw new IllegalArgumentException("Từ vựng không đủ dữ liệu để sinh câu hỏi");
        }
        Collections.shuffle(supportedTypes);
        return create(vocab, supportedTypes.get(0));
    }

    /* default */ QuestionResult create(Vocab vocab, QuestionType questionType) {
        if (!questionType.supports(vocab)) {
            throw new IllegalArgumentException("Từ vựng không đủ dữ liệu cho loại câu hỏi đã chọn");
        }

        Question question = Question.builder()
                .vocab(vocab)
                .questionContent(questionType.questionContent(vocab))
                .correctAnswer(questionType.correctAnswer(vocab))
                .audioUrl(questionType.questionAudioUrl(vocab))
                .build();
        return new QuestionResult(question, questionType);
    }

    /* default */ List<QuestionType> supportedTypes(Vocab vocab) {
        List<QuestionType> supportedTypes = new ArrayList<>();
        for (QuestionType questionType : QuestionType.values()) {
            if (questionType.supports(vocab)) {
                supportedTypes.add(questionType);
            }
        }
        return supportedTypes;
    }

    @Getter
    @AllArgsConstructor
    public static class QuestionResult {
        private Question question;
        private QuestionType questionType;
    }
}
