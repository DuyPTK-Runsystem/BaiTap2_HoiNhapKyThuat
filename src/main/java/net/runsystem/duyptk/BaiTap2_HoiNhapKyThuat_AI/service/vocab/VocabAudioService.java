package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.service.vocab;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.service.tts.GoogleTtsService;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.util.error.ExternalServerException;

@Service
@RequiredArgsConstructor
public class VocabAudioService {
    private static final String DEFAULT_AUDIO_BASE_URL = "/api/v1/vocabs/audio";
    private static final String DEFAULT_AUDIO_STORAGE_DIR = "build/vocab-audio";
    private static final String DEFAULT_TTS_LANGUAGE_CODE = "en-GB";
    private static final String MP3_EXTENSION = ".mp3";
    private static final String SAFE_AUDIO_FILE_PATTERN = "[a-z0-9][a-z0-9-]*-[a-f0-9-]{36}\\.mp3";

    private final GoogleTtsService googleTtsService;

    @Value("${app.vocab.audio-storage-dir:}")
    private String audioStorageDir;

    @Value("${app.vocab.audio-base-url:}")
    private String audioBaseUrl;

    @Value("${app.vocab.tts-language-code:}")
    private String ttsLanguageCode;

    public String synthesizeAndStoreAudio(String word, String ipa) {
        try {
            byte[] audioBytes = googleTtsService.synthesizeIpa(word, ipa, resolvedTtsLanguageCode());
            if (audioBytes == null || audioBytes.length == 0) {
                throw new ExternalServerException("Google TTS không trả dữ liệu audio");
            }

            String fileName = audioFileName(word);
            Files.createDirectories(audioStoragePath());
            Files.write(audioStoragePath().resolve(fileName), audioBytes);
            return resolvedAudioBaseUrl() + "/" + fileName;
        } catch (IOException exception) {
            throw new ExternalServerException("Không thể lưu file audio", exception);
        }
    }

    public byte[] readAudioFile(String fileName) {
        if (fileName == null || !fileName.matches(SAFE_AUDIO_FILE_PATTERN)) {
            throw new IllegalArgumentException("Tên file audio không hợp lệ");
        }

        Path audioFile = audioStoragePath().resolve(fileName).normalize();
        if (!audioFile.startsWith(audioStoragePath())) {
            throw new IllegalArgumentException("Tên file audio không hợp lệ");
        }

        try {
            return Files.readAllBytes(audioFile);
        } catch (IOException exception) {
            throw new ExternalServerException("Không thể đọc file audio", exception);
        }
    }

    private String audioFileName(String word) {
        return safeWordSlug(word) + "-" + UUID.randomUUID() + MP3_EXTENSION;
    }

    private String safeWordSlug(String word) {
        String slug = word.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        if (slug.isBlank()) {
            return "vocab";
        }
        return slug;
    }

    private Path audioStoragePath() {
        return Path.of(configValue(audioStorageDir, DEFAULT_AUDIO_STORAGE_DIR))
                .toAbsolutePath()
                .normalize();
    }

    private String resolvedAudioBaseUrl() {
        String value = configValue(audioBaseUrl, DEFAULT_AUDIO_BASE_URL);
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }

    private String resolvedTtsLanguageCode() {
        return configValue(ttsLanguageCode, DEFAULT_TTS_LANGUAGE_CODE);
    }

    private String configValue(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.trim();
    }
}
