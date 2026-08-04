package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.service.vocab;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository.VocabRepository;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.util.error.IdInvalidException;

@Service
@RequiredArgsConstructor
public class VocabValidationService {
    private final VocabRepository vocabRepository;

    public String requireWord(String word) {
        if (word == null || word.isBlank()) {
            throw new IdInvalidException("Word không được để trống");
        }
        return word.trim();
    }

    public void validateUniqueWord(String word) {
        if (vocabRepository.existsByWord(word)) {
            throw new IdInvalidException("Từ vựng đã tồn tại");
        }
    }

    public void validateManualImport(String meaning, String ipa) {
        if (ipa == null || isBlank(meaning)) {
            throw new IdInvalidException("Nếu không tìm thấy IPA, Word, Meaning và IPA là bắt buộc");
        }
    }

    public String requireMeaning(String meaning) {
        if (meaning == null || meaning.isBlank()) {
            throw new IdInvalidException("Meaning không được để trống");
        }
        return meaning.trim();
    }

    public String blankToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
