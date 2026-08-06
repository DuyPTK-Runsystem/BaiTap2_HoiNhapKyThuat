package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.service.vocab;

import java.util.Optional;

import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.externalDTO.VocabAutomationResult;

@FunctionalInterface
public interface VocabAutomationService {
    Optional<VocabAutomationResult> resolve(String word);
}
