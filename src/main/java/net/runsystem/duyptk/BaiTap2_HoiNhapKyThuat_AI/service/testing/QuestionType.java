package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.service.testing;

import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.Vocab;

public enum QuestionType {
    MEANING_OF_WORD,
    PRONUNCIATION_OF_WORD,
    WORD_BY_MEANING,
    WORD_BY_MEANING_AND_AUDIO,
    WORD_BY_AUDIO;

    public boolean supports(Vocab vocab) {
        return switch (this) {
            case MEANING_OF_WORD -> hasText(vocab.getWord()) && hasText(vocab.getMeaning());
            case PRONUNCIATION_OF_WORD -> hasText(vocab.getWord()) && hasText(vocab.getAudioUrl());
            case WORD_BY_MEANING -> hasText(vocab.getWord()) && hasText(vocab.getMeaning());
            case WORD_BY_MEANING_AND_AUDIO -> hasText(vocab.getWord())
                    && hasText(vocab.getMeaning())
                    && hasText(vocab.getAudioUrl());
            case WORD_BY_AUDIO -> hasText(vocab.getWord()) && hasText(vocab.getAudioUrl());
        };
    }

    public String questionContent(Vocab vocab) {
        return switch (this) {
            case MEANING_OF_WORD -> "Từ '" + vocab.getWord() + "' có ý nghĩa gì?";
            case PRONUNCIATION_OF_WORD -> "Từ '" + vocab.getWord() + "' phát âm như thế nào?";
            case WORD_BY_MEANING, WORD_BY_MEANING_AND_AUDIO ->
                    "Từ nào có ý nghĩa là '" + vocab.getMeaning() + "'?";
            case WORD_BY_AUDIO -> "Từ nào có phát âm như sau?";
        };
    }

    public String correctAnswer(Vocab vocab) {
        return switch (this) {
            case MEANING_OF_WORD -> vocab.getMeaning();
            case PRONUNCIATION_OF_WORD -> vocab.getAudioUrl();
            case WORD_BY_MEANING, WORD_BY_MEANING_AND_AUDIO, WORD_BY_AUDIO -> vocab.getWord();
        };
    }

    public String questionAudioUrl(Vocab vocab) {
        return switch (this) {
            case PRONUNCIATION_OF_WORD, WORD_BY_MEANING_AND_AUDIO, WORD_BY_AUDIO -> vocab.getAudioUrl();
            default -> null;
        };
    }

    public String optionAudioUrl(Vocab vocab) {
        if (this == PRONUNCIATION_OF_WORD) {
            return vocab.getAudioUrl();
        }
        return null;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
