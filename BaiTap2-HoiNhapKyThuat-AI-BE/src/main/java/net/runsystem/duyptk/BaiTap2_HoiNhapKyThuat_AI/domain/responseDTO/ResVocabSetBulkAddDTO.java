package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.responseDTO;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResVocabSetBulkAddDTO {
    private ResVocabSetSummaryDTO vocabSet;
    private Integer total;
    private Integer success;
    private Integer failed;
    private List<ResVocabSetBulkAddItemDTO> items;
}
