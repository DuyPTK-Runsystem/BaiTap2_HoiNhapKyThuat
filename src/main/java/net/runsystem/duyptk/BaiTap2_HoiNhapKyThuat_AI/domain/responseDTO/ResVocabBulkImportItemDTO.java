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
public class ResVocabBulkImportItemDTO {
    @JsonProperty("row_number")
    private int rowNumber;

    private String word;
    private boolean success;
    private ResVocabDTO vocab;
    private String error;
}
