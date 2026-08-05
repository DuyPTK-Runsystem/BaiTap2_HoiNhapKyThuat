package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResVocabSetSummaryDTO {
    private Long id;
    private String name;
    private String description;
    private Long parentId;
    private Integer vocabCount;
}
