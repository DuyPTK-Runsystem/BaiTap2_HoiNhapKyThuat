package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.service.testing;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.Option;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.Question;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.Vocab;

class OptionGeneratorTests {
    private static final String APPLE = "apple";
    private static final String APPLE_MEANING = "quả táo";

    private final OptionGenerator optionGenerator = new OptionGenerator();

    @Test
    void shouldGenerateExactlyFourOptionsWithOneCorrectAnswer() {
        Question question = question(vocab(1L, APPLE, APPLE_MEANING));

        List<Option> options = optionGenerator.generate(
                question,
                QuestionType.MEANING_OF_WORD,
                List.of(
                        vocab(1L, APPLE, APPLE_MEANING),
                        vocab(2L, "book", "quyển sách"),
                        vocab(3L, "cat", "con mèo"),
                        vocab(4L, "dog", "con chó")));

        Assertions.assertThat(options).satisfies(generatedOptions -> {
            if (generatedOptions.size() != 4) {
                throw new AssertionError("Mỗi câu hỏi phải có đúng 4 option");
            }
            if (!generatedOptions.stream().map(Option::getOptionOrder).toList().equals(List.of(1, 2, 3, 4))) {
                throw new AssertionError("Option order phải là 1, 2, 3, 4");
            }
            List<? extends Option> correctOptions = generatedOptions.stream()
                    .filter(Option::isCorrect)
                    .toList();
            if (correctOptions.size() != 1 || !APPLE_MEANING.equals(correctOptions.get(0).getOptionContent())) {
                throw new AssertionError("Phải có đúng một option đúng khớp correct answer");
            }
        });
    }

    @Test
    void shouldRejectWhenDistractorsAreInsufficient() {
        Question question = question(vocab(1L, APPLE, APPLE_MEANING));

        Assertions.assertThatThrownBy(() -> optionGenerator.generate(
                        question,
                        QuestionType.MEANING_OF_WORD,
                        List.of(
                                vocab(1L, APPLE, APPLE_MEANING),
                                vocab(2L, "book", "quyển sách"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Không đủ đáp án nhiễu duy nhất để tạo câu hỏi");
    }

    @Test
    void shouldUseAudioUrlForPronunciationOptions() {
        Vocab root = vocab(1L, APPLE, APPLE_MEANING, "apple.mp3");
        Question question = Question.builder()
                .vocab(root)
                .correctAnswer(root.getAudioUrl())
                .audioUrl(root.getAudioUrl())
                .build();

        List<Option> options = optionGenerator.generate(
                question,
                QuestionType.PRONUNCIATION_OF_WORD,
                List.of(
                        root,
                        vocab(2L, "book", "quyển sách", "book.mp3"),
                        vocab(3L, "cat", "con mèo", "cat.mp3"),
                        vocab(4L, "dog", "con chó", "dog.mp3")));

        Assertions.assertThat(options).allSatisfy(option ->
                Assertions.assertThat(option.getAudioUrl()).isNotBlank());
    }

    private Question question(Vocab vocab) {
        return Question.builder()
                .vocab(vocab)
                .correctAnswer(vocab.getMeaning())
                .build();
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
