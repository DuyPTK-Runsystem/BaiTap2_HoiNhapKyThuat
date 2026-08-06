package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResVocabSetVocabDTO {
    private ResVocabSetSummaryDTO vocabSet;
    private ResVocabDTO vocab;
    private Boolean added;
}
