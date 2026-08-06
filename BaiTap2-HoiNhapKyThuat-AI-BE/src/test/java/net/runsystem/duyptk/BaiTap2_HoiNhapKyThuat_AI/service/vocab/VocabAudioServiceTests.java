package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.service.vocab;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.service.tts.GoogleTtsService;

class VocabAudioServiceTests {
    private static final byte[] AUDIO_BYTES = {1, 2, 3};
    private static final String HELLO = "hello";
    private static final String IPA = "/həˈləʊ/";

    @Test
    void synthesizeAndStoreAudioShouldCallGoogleTtsService() throws Exception {
        GoogleTtsService googleTtsService = Mockito.mock(GoogleTtsService.class);
        Mockito.when(googleTtsService.synthesizeIpa(HELLO, IPA, "en-GB")).thenReturn(AUDIO_BYTES);
        VocabAudioService vocabAudioService = new VocabAudioService(googleTtsService);

        vocabAudioService.synthesizeAndStoreAudio(HELLO, IPA);

        Mockito.verify(googleTtsService).synthesizeIpa(HELLO, IPA, "en-GB");
    }

    @Test
    void synthesizeAndStoreAudioShouldReturnAudioUrl() throws Exception {
        GoogleTtsService googleTtsService = Mockito.mock(GoogleTtsService.class);
        Mockito.when(googleTtsService.synthesizeIpa(HELLO, IPA, "en-GB")).thenReturn(AUDIO_BYTES);
        VocabAudioService vocabAudioService = new VocabAudioService(googleTtsService);

        String audioUrl = vocabAudioService.synthesizeAndStoreAudio(HELLO, IPA);

        Assertions.assertThat(audioUrl).matches("/api/v1/vocabs/audio/hello-[a-f0-9-]{36}\\.mp3");
    }
}
