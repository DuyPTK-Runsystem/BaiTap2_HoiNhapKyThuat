package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.responseDTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.service.testing.FlashcardFrontType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResFlashcardDTO {
    private Long vocabId;
    private FlashcardFrontType frontType;
    private String frontText;
    private String frontAudioUrl;
    private String backWord;
    private String backMeaning;
    private String backAudioUrl;
}
