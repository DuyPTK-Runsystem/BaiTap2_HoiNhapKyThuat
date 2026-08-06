package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.service.vocab;

import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.externalDTO.VocabAutomationResult;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.util.error.ExternalServerException;

@Service
@RequiredArgsConstructor
public class FreeDictionaryVocabAutomationService implements VocabAutomationService {
    private static final String IPA_TYPE = "ipa";
    private static final String PRONUNCIATIONS_FIELD = "pronunciations";
    private static final String TEXT_FIELD = "text";
    private static final String TYPE_FIELD = "type";

    private final RestClient.Builder restClientBuilder;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.free-dictionary.base-url}")
    private String baseUrl;

    @Value("${app.free-dictionary.language}")
    private String language;

    @Override
    public Optional<VocabAutomationResult> resolve(String word) {
        if (word == null || word.isBlank()) {
            return Optional.empty();
        }

        return parseFreeDictionaryResponse(fetchDictionaryEntry(word.trim()));
    }

    public Optional<VocabAutomationResult> parseFreeDictionaryResponse(JsonNode response) {
        return findFirstIpa(response);
    }

    private JsonNode fetchDictionaryEntry(String word) {
        try {
            String responseBody = restClientBuilder.build()
                    .get()
                    .uri(baseUrl, language, word)
                    .retrieve()
                    .body(String.class);
            return objectMapper.readTree(responseBody);
        } catch (RestClientException exception) {
            throw new RestClientException("Không thể gọi Free Dictionary API: " + exception.getMessage(), exception);
        } catch (JsonProcessingException exception) {
            throw new ExternalServerException("Free Dictionary API trả JSON không hợp lệ", exception);
        }
    }

    private Optional<VocabAutomationResult> findFirstIpa(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return Optional.empty();
        }

        Optional<VocabAutomationResult> objectResult = findIpaInObject(node);
        if (objectResult.isPresent()) {
            return objectResult;
        }

        return findIpaInChildren(node);
    }

    private Optional<VocabAutomationResult> findIpaInObject(JsonNode node) {
        if (!node.isObject() || !node.has(PRONUNCIATIONS_FIELD)) {
            return Optional.empty();
        }

        return firstIpaFromPronunciations(node.get(PRONUNCIATIONS_FIELD))
                .map(this::toAutomationResult);
    }

    private Optional<VocabAutomationResult> findIpaInChildren(JsonNode node) {
        if (!node.isContainerNode()) {
            return Optional.empty();
        }

        for (JsonNode child : node) {
            Optional<VocabAutomationResult> result = findFirstIpa(child);
            if (result.isPresent()) {
                return result;
            }
        }

        return Optional.empty();
    }

    private Optional<String> firstIpaFromPronunciations(JsonNode pronunciations) {
        if (pronunciations == null || !pronunciations.isArray()) {
            return Optional.empty();
        }

        for (JsonNode pronunciation : pronunciations) {
            String type = textValue(pronunciation, TYPE_FIELD);
            String text = textValue(pronunciation, TEXT_FIELD);
            if (IPA_TYPE.equalsIgnoreCase(type) && text != null) {
                return Optional.of(text);
            }
        }

        return Optional.empty();
    }

    private VocabAutomationResult toAutomationResult(String ipa) {
        return VocabAutomationResult.builder()
                .ipa(ipa)
                .build();
    }

    private String textValue(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        if (value == null || !value.isTextual() || value.asText().isBlank()) {
            return null;
        }
        return value.asText().trim();
    }
}
