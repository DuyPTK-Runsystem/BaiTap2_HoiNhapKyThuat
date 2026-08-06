package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.Folder;

public interface FolderRepository extends JpaRepository<Folder, Long> {
    Optional<Folder> findByIdAndUserId(Long id, Long userId);

    List<Folder> findByUserIdAndFolderNameContainingIgnoreCaseOrderByIdAsc(Long userId, String folderName);

    List<Folder> findByUserIdAndParentIsNullAndFolderName(Long userId, String folderName);

    List<Folder> findByUserIdAndParentIdAndFolderName(Long userId, Long parentId, String folderName);
}
