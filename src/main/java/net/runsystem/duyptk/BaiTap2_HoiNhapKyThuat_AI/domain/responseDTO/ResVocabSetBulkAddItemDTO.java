package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResVocabSetBulkAddItemDTO {
    private Long vocabId;
    private Boolean success;
    private Boolean added;
    private ResVocabDTO vocab;
    private String error;
}
