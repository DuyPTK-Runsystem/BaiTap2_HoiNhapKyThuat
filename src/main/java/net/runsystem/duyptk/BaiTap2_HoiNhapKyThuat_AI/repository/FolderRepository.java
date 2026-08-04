package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.Folder;

public interface FolderRepository extends JpaRepository<Folder, Long> {
    Optional<Folder> findByIdAndUserId(Long id, Long userId);
}
