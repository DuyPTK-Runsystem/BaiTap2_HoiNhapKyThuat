package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.service.tts;

import java.io.IOException;

import com.google.cloud.texttospeech.v1.AudioConfig;
import com.google.cloud.texttospeech.v1.AudioEncoding;
import com.google.cloud.texttospeech.v1.SynthesisInput;
import com.google.cloud.texttospeech.v1.SynthesizeSpeechResponse;
import com.google.cloud.texttospeech.v1.TextToSpeechClient;
import com.google.cloud.texttospeech.v1.VoiceSelectionParams;
import org.springframework.stereotype.Service;

@Service
public class GoogleTtsService {

    public byte[] synthesizeIpa(
            String word,
            String ipa,
            String languageCode) throws IOException {
        validateInput(word, ipa);

        String normalizedIpa = normalizeIpa(ipa);
        String normalizedLanguageCode = normalizeLanguageCode(languageCode);

        String ssml = """
                <speak>
                    <phoneme alphabet="ipa" ph="%s">%s</phoneme>
                </speak>
                """.formatted(
                escapeXmlAttribute(normalizedIpa),
                escapeXmlText(word.trim()));

        SynthesisInput input = SynthesisInput.newBuilder()
                .setSsml(ssml)
                .build();

        VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                .setLanguageCode(normalizedLanguageCode)
                .build();

        AudioConfig audioConfig = AudioConfig.newBuilder()
                .setAudioEncoding(AudioEncoding.MP3)
                .build();

        try (TextToSpeechClient client = TextToSpeechClient.create()) {
            SynthesizeSpeechResponse response = client.synthesizeSpeech(input, voice, audioConfig);

            return response.getAudioContent().toByteArray();
        }
    }

    private void validateInput(String word, String ipa) {
        if (word == null || word.isBlank()) {
            throw new IllegalArgumentException("word must not be blank");
        }

        if (ipa == null || ipa.isBlank()) {
            throw new IllegalArgumentException("ipa must not be blank");
        }
    }

    private String normalizeIpa(String ipa) {
        String result = ipa.trim();

        if (result.startsWith("/") && result.endsWith("/") && result.length() > 1) {
            result = result.substring(1, result.length() - 1);
        }

        /*
         * Dấu chấm chỉ ranh giới âm tiết.
         * Có thể bỏ để giảm nguy cơ voice xử lý không đúng.
         */
        return result.replace(".", "");
    }

    private String normalizeLanguageCode(String languageCode) {
        if (languageCode == null || languageCode.isBlank()) {
            return "en-GB";
        }

        return languageCode.trim();
    }

    private String escapeXmlAttribute(String value) {
        return value
                .replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private String escapeXmlText(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
