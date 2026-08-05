package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.service.testing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.Option;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.Question;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.Vocab;

@Component
public class OptionGenerator {
    private static final int REQUIRED_OPTION_COUNT = 4;
    private static final int REQUIRED_DISTRACTOR_COUNT = 3;

    public List<Option> generate(Question question, QuestionType questionType, List<Vocab> sourceVocabs) {
        List<OptionCandidate> candidates = distractorCandidates(question, questionType, sourceVocabs);
        if (candidates.size() < REQUIRED_DISTRACTOR_COUNT) {
            throw new IllegalArgumentException("Không đủ đáp án nhiễu duy nhất để tạo câu hỏi");
        }

        Collections.shuffle(candidates);
        List<OptionCandidate> selectedCandidates = new ArrayList<>(candidates.subList(0, REQUIRED_DISTRACTOR_COUNT));
        selectedCandidates.add(new OptionCandidate(question.getCorrectAnswer(), question.getAudioUrl(), true));
        Collections.shuffle(selectedCandidates);
        return buildOptions(question, selectedCandidates);
    }

    public boolean hasEnoughDistractors(Question question, QuestionType questionType, List<Vocab> sourceVocabs) {
        return distractorCandidates(question, questionType, sourceVocabs).size() >= REQUIRED_DISTRACTOR_COUNT;
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private List<OptionCandidate> distractorCandidates(
            Question question,
            QuestionType questionType,
            List<Vocab> sourceVocabs) {
        Map<String, OptionCandidate> candidates = new LinkedHashMap<>();
        for (Vocab vocab : sourceVocabs) {
            if (question.getVocab().getId().equals(vocab.getId()) || !questionType.supports(vocab)) {
                continue;
            }
            String optionContent = questionType.correctAnswer(vocab);
            if (!question.getCorrectAnswer().equals(optionContent)) {
                candidates.putIfAbsent(
                        optionContent,
                        new OptionCandidate(optionContent, questionType.optionAudioUrl(vocab), false));
            }
        }
        return new ArrayList<>(candidates.values());
    }

    private List<Option> buildOptions(Question question, List<OptionCandidate> selectedCandidates) {
        List<Option> options = new ArrayList<>();
        for (int index = 0; index < REQUIRED_OPTION_COUNT; index++) {
            OptionCandidate candidate = selectedCandidates.get(index);
            options.add(Option.builder()
                    .question(question)
                    .optionOrder(index + 1)
                    .optionContent(candidate.optionContent)
                    .correct(candidate.correct)
                    .audioUrl(candidate.audioUrl)
                    .build());
        }
        return options;
    }

    private static class OptionCandidate {
        private final String optionContent;
        private final String audioUrl;
        private final boolean correct;

        /* default */ OptionCandidate(String optionContent, String audioUrl, boolean correct) {
            this.optionContent = optionContent;
            this.audioUrl = audioUrl;
            this.correct = correct;
        }
    }
}
