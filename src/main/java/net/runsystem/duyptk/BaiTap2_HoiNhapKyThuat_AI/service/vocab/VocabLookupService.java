package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.service.vocab;

import java.util.NoSuchElementException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.Vocab;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository.VocabRepository;

@Service
@RequiredArgsConstructor
public class VocabLookupService {
    private final VocabRepository vocabRepository;
    private final VocabValidationService vocabValidationService;

    public Vocab findByIdOrWord(Long id, String word) {
        if (id != null) {
            return vocabRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("Không tìm thấy từ vựng"));
        }

        String normalizedWord = vocabValidationService.requireWord(word);
        return vocabRepository.findByWord(normalizedWord)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy từ vựng"));
    }
}
