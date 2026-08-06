package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.responseDTO;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResVocabBulkImportDTO {
    @JsonProperty("total_rows")
    private int totalRows;

    @JsonProperty("success_count")
    private int successCount;

    @JsonProperty("failure_count")
    private int failureCount;

    private List<ResVocabBulkImportItemDTO> items;
}
