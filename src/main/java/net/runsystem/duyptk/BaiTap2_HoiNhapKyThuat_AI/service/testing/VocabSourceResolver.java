package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.service.testing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.Item;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.User;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.Vocab;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository.ItemRepository;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository.UserRepository;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository.VocabRepository;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.util.SecurityUtil;

@Service
@RequiredArgsConstructor
public class VocabSourceResolver {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final VocabRepository vocabRepository;

    @Transactional(readOnly = true)
    public List<Vocab> resolve(List<Long> sourceItemIds, int numberOfQuestion) {
        validateQuestionCount(numberOfQuestion);
        User user = currentUser();
        if (sourceItemIds == null || sourceItemIds.isEmpty()) {
            return randomVocabs(numberOfQuestion);
        }

        List<Long> normalizedIds = normalizeSourceIds(sourceItemIds);
        List<Item> ownedItems = itemRepository.findByIdInAndUserId(normalizedIds, user.getId());
        if (ownedItems.size() != normalizedIds.size()) {
            throw new NoSuchElementException("Nguồn bài test không tồn tại hoặc không thuộc người dùng hiện tại");
        }

        List<Vocab> sourceVocabs = vocabRepository.findBySourceItemIdsAndUserId(normalizedIds, user.getId());
        return selectVocabs(sourceVocabs, numberOfQuestion);
    }

    private User currentUser() {
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy người dùng hiện tại"));
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy người dùng hiện tại"));
    }

    private void validateQuestionCount(int numberOfQuestion) {
        if (numberOfQuestion < 1) {
            throw new IllegalArgumentException("Số lượng câu hỏi phải lớn hơn hoặc bằng 1");
        }
    }

    private List<Long> normalizeSourceIds(List<Long> sourceItemIds) {
        if (sourceItemIds.stream().anyMatch(id -> id == null || id < 1)) {
            throw new IllegalArgumentException("Source item id phải lớn hơn 0");
        }
        Set<Long> uniqueIds = new LinkedHashSet<>(sourceItemIds);
        return new ArrayList<>(uniqueIds);
    }

    private List<Vocab> randomVocabs(int numberOfQuestion) {
        List<Vocab> vocabs = vocabRepository.findRandom(PageRequest.of(0, numberOfQuestion));
        if (vocabs.size() < numberOfQuestion) {
            throw new IllegalArgumentException("Không đủ từ vựng để tạo bài test");
        }
        return vocabs;
    }

    private List<Vocab> selectVocabs(List<Vocab> sourceVocabs, int numberOfQuestion) {
        if (sourceVocabs.size() < numberOfQuestion) {
            throw new IllegalArgumentException("Nguồn không có đủ từ vựng để tạo bài test");
        }
        List<Vocab> selectedVocabs = new ArrayList<>(sourceVocabs);
        Collections.shuffle(selectedVocabs);
        return List.copyOf(selectedVocabs.subList(0, numberOfQuestion));
    }
}
