package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.responseDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResVocabDTO {
    private Long id;
    private String word;
    private String meaning;
    private String ipa;

    @JsonProperty("audio_url")
    private String audioUrl;

    private boolean mastered;
}
