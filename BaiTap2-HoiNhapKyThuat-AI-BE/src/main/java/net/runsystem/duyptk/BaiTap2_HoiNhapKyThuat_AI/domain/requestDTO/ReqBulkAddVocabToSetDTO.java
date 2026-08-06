package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.requestDTO;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReqBulkAddVocabToSetDTO {
    private List<Long> vocabIds;
}
