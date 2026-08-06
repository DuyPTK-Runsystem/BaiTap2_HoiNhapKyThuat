package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.service.testing;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.Question;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.Vocab;

class QuestionFactoryTests {
    private static final String WORD = "apple";
    private static final String MEANING = "quả táo";
    private static final String AUDIO_URL = "/api/v1/vocabs/audio/apple.mp3";

    private final QuestionFactory questionFactory = new QuestionFactory();

    @Test
    void shouldCreateMeaningOfWordQuestion() {
        Question question = questionFactory.create(vocab(), QuestionType.MEANING_OF_WORD).getQuestion();

        Assertions.assertThat(question)
                .extracting(Question::getQuestionContent, Question::getCorrectAnswer, Question::getAudioUrl)
                .containsExactly("Từ 'apple' có ý nghĩa gì?", MEANING, null);
    }

    @Test
    void shouldCreatePronunciationQuestion() {
        Question question = questionFactory.create(vocab(), QuestionType.PRONUNCIATION_OF_WORD).getQuestion();

        Assertions.assertThat(question)
                .extracting(Question::getQuestionContent, Question::getCorrectAnswer, Question::getAudioUrl)
                .containsExactly("Từ 'apple' phát âm như thế nào?", AUDIO_URL, AUDIO_URL);
    }

    @Test
    void shouldCreateWordByMeaningQuestion() {
        Question question = questionFactory.create(vocab(), QuestionType.WORD_BY_MEANING).getQuestion();

        Assertions.assertThat(question)
                .extracting(Question::getQuestionContent, Question::getCorrectAnswer, Question::getAudioUrl)
                .containsExactly("Từ nào có ý nghĩa là 'quả táo'?", WORD, null);
    }

    @Test
    void shouldCreateWordByMeaningAndAudioQuestion() {
        Question question = questionFactory.create(vocab(), QuestionType.WORD_BY_MEANING_AND_AUDIO).getQuestion();

        Assertions.assertThat(question)
                .extracting(Question::getQuestionContent, Question::getCorrectAnswer, Question::getAudioUrl)
                .containsExactly("Từ nào có ý nghĩa là 'quả táo'?", WORD, AUDIO_URL);
    }

    @Test
    void shouldCreateWordByAudioQuestion() {
        Question question = questionFactory.create(vocab(), QuestionType.WORD_BY_AUDIO).getQuestion();

        Assertions.assertThat(question)
                .extracting(Question::getQuestionContent, Question::getCorrectAnswer, Question::getAudioUrl)
                .containsExactly("Từ nào có phát âm như sau?", WORD, AUDIO_URL);
    }

    @Test
    void shouldRejectUnsupportedQuestionType() {
        Vocab missingAudio = Vocab.builder()
                .id(1L)
                .word(WORD)
                .meaning(MEANING)
                .build();

        Assertions.assertThatThrownBy(() -> questionFactory.create(missingAudio, QuestionType.WORD_BY_AUDIO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Từ vựng không đủ dữ liệu cho loại câu hỏi đã chọn");
    }

    private Vocab vocab() {
        return Vocab.builder()
                .id(1L)
                .word(WORD)
                .meaning(MEANING)
                .audioUrl(AUDIO_URL)
                .build();
    }
}
