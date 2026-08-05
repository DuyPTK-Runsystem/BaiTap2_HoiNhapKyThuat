package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.Vocab;

public interface VocabRepository extends JpaRepository<Vocab, Long> {
    @Query(value = """
            WITH RECURSIVE item_tree AS (
                SELECT item_id, type, user_id
                FROM items
                WHERE item_id IN (:sourceItemIds) AND user_id = :userId
                UNION ALL
                SELECT child.item_id, child.type, child.user_id
                FROM items child
                JOIN item_tree parent ON child.parent_id = parent.item_id
                WHERE child.user_id = :userId
            )
            SELECT DISTINCT vocab.*
            FROM item_tree
            JOIN vocab_vocab_set ON vocab_vocab_set.vocab_set_id = item_tree.item_id
            JOIN vocabs vocab ON vocab.vocab_id = vocab_vocab_set.vocab_id
            WHERE item_tree.type = 'VOCAB_SET'
            """, nativeQuery = true)
    List<Vocab> findBySourceItemIdsAndUserId(
            @Param("sourceItemIds") List<Long> sourceItemIds,
            @Param("userId") Long userId);

    @Query(value = "SELECT * FROM vocabs ORDER BY RAND()", nativeQuery = true)
    List<Vocab> findRandom(Pageable pageable);

    boolean existsByWord(String word);

    Optional<Vocab> findByWord(String word);
}
