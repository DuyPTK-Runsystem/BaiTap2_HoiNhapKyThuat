package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.requestDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReqCreateVocabDTO {
    private String word;
    private String meaning;
    private String ipa;
}
