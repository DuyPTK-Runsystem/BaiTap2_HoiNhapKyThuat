package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.VocabSet;

public interface VocabSetRepository extends JpaRepository<VocabSet, Long> {
    Optional<VocabSet> findByIdAndUserId(Long id, Long userId);
}
