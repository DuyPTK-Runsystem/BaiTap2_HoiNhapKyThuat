package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.externalDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VocabAutomationResult {
    private String ipa;
    private String audioUrl;
}
