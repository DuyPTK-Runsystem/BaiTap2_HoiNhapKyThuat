package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.service.vocab;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.externalDTO.VocabAutomationResult;

class FreeDictionaryVocabAutomationServiceTests {
    @Test
    void parseShouldSelectFirstIpaPronunciation() throws Exception {
        FreeDictionaryVocabAutomationService automationService = new FreeDictionaryVocabAutomationService(
                RestClient.builder());
        VocabAutomationResult result = automationService
                .parseFreeDictionaryResponse(new ObjectMapper().readTree("""
                        {
                          "word": "hello",
                          "entries": [
                            {
                              "pronunciations": [
                                {
                                  "type": "respelling",
                                  "text": "heh-loh"
                                },
                                {
                                  "type": "ipa",
                                  "text": "/həˈləʊ/"
                                },
                                {
                                  "type": "ipa",
                                  "text": "/second/"
                                }
                              ]
                            }
                          ]
                        }
                        """)).orElseThrow();

        Assertions.assertThat(result)
                .extracting(
                        VocabAutomationResult::getIpa,
                        VocabAutomationResult::getAudioUrl)
                .containsExactly(
                        "/həˈləʊ/",
                        null);
    }
}
