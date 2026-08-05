package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.Vocab;

public interface VocabRepository extends JpaRepository<Vocab, Long> {
    boolean existsByWord(String word);

    Optional<Vocab> findByWord(String word);
}
