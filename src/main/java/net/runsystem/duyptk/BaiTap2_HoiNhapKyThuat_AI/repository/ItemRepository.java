package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.Item;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByUserIdAndParentIsNullOrderByIdAsc(Long userId);

    List<Item> findByUserIdAndParentIdOrderByIdAsc(Long userId, Long parentId);
}
