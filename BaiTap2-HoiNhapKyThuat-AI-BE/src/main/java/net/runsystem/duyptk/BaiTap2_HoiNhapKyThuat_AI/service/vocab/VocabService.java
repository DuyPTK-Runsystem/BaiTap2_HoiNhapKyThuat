package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.service.vocab;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.externalDTO.VocabAutomationResult;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.requestDTO.ReqCreateVocabDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.requestDTO.ReqUpdateVocabDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.responseDTO.ResVocabDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.responseDTO.ResVocabSetVocabDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.responseDTO.ResultPaginationDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.Vocab;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.VocabSet;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository.VocabRepository;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.service.organization.VocabSetMembershipService;

@Service
@RequiredArgsConstructor
public class VocabService {
    private final VocabRepository vocabRepository;
    private final VocabAutomationService vocabAutomationService;
    private final VocabAudioService vocabAudioService;
    private final VocabLookupService vocabLookupService;
    private final VocabValidationService vocabValidationService;
    private final VocabSetMembershipService vocabSetMembershipService;

    @Transactional
    public ResVocabDTO create(ReqCreateVocabDTO request) {
        return convertToDTO(createEntity(request));
    }

    @Transactional
    public ResVocabSetVocabDTO create(ReqCreateVocabDTO request, Long vocabSetId) {
        vocabSetMembershipService.validateVocabSetAccess(vocabSetId);
        Vocab vocab = createEntity(request);
        return vocabSetMembershipService.addVocabToSet(vocabSetId, vocab);
    }

    @Transactional
    public Vocab createEntity(ReqCreateVocabDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Request tạo từ vựng không được để trống");
        }

        String word = request.getWord();
        String meaning = request.getMeaning();
        String normalizedWord = vocabValidationService.requireWord(word);
        vocabValidationService.validateUniqueWord(normalizedWord);
        VocabAutomationResult automationResult = resolveAutomationResult(normalizedWord, request.getIpa());
        String resolvedIpa = firstPresent(request.getIpa(), automationResult.getIpa());

        vocabValidationService.validateManualImport(meaning, resolvedIpa);
        String resolvedAudioUrl = resolveAudioUrl(normalizedWord, resolvedIpa, automationResult);

        Vocab vocab = Vocab.builder()
                .word(normalizedWord)
                .meaning(vocabValidationService.blankToNull(meaning))
                .ipa(resolvedIpa)
                .audioUrl(resolvedAudioUrl)
                .build();

        return vocabRepository.save(vocab);
    }

    @Transactional(readOnly = true)
    public ResVocabDTO get(Long id, String word) {
        return convertToDTO(vocabLookupService.findByIdOrWord(id, word));
    }

    public static Specification<Vocab> belongsToVocabSet(Long vocabSetId) {
        return (root, query, cb) -> {
            Subquery<Long> subquery = query.subquery(Long.class);

            Root<VocabSet> vocabSetRoot = subquery.from(VocabSet.class);
            Join<VocabSet, Vocab> vocabJoin = vocabSetRoot.join("vocabs");

            subquery.select(vocabJoin.get("id"))
                    .where(cb.equal(vocabSetRoot.get("id"), vocabSetId));

            return root.get("id").in(subquery);
        };
    }

    public ResultPaginationDTO getAll(Specification<Vocab> specification, Pageable pageable) {
        Page<Vocab> vocabs = vocabRepository.findAll(specification, pageable);
        ResultPaginationDTO result = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta()
                .builder()
                .page(pageable.getPageNumber() + 1)
                .pageSize(pageable.getPageSize())
                .totalPages(vocabs.getTotalPages())
                .totalItems(vocabs.getTotalElements())
                .build();
        result.setMeta(meta);
        result.setResult(vocabs.getContent());
        return result;
    }

    public ResultPaginationDTO getAll(Long vocabSetId, Specification<Vocab> specification, Pageable pageable) {
        Specification<Vocab> finalSpec = belongsToVocabSet(vocabSetId).and(specification);
        Page<Vocab> vocabs = vocabRepository.findAll(finalSpec, pageable);
        ResultPaginationDTO result = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta()
                .builder()
                .page(pageable.getPageNumber() + 1)
                .pageSize(pageable.getPageSize())
                .totalPages(vocabs.getTotalPages())
                .totalItems(vocabs.getTotalElements())
                .build();
        result.setMeta(meta);
        result.setResult(vocabs.getContent());
        return result;
    }

    @Transactional
    public ResVocabDTO update(Long id, String word, ReqUpdateVocabDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cập nhật từ vựng không được để trống");
        }

        String meaning = vocabValidationService.requireMeaning(request.getMeaning());
        Vocab vocab = vocabLookupService.findByIdOrWord(id, word);
        vocab.setMeaning(meaning);
        return convertToDTO(vocabRepository.save(vocab));
    }

    public ResVocabDTO convertToDTO(Vocab vocab) {
        if (vocab == null) {
            return null;
        }

        return ResVocabDTO.builder()
                .id(vocab.getId())
                .word(vocab.getWord())
                .meaning(vocab.getMeaning())
                .ipa(vocab.getIpa())
                .audioUrl(vocab.getAudioUrl())
                .mastered(vocab.isMastered())
                .build();
    }

    public byte[] readAudioFile(String fileName) {
        return vocabAudioService.readAudioFile(fileName);
    }

    private VocabAutomationResult resolveAutomationResult(String word, String ipa) {
        if (vocabValidationService.blankToNull(ipa) != null) {
            return new VocabAutomationResult();
        }
        return vocabAutomationService.resolve(word).orElseGet(VocabAutomationResult::new);
    }

    private String resolveAudioUrl(
            String word,
            String ipa,
            VocabAutomationResult automationResult) {
        String audioUrl = vocabValidationService.blankToNull(automationResult.getAudioUrl());
        if (audioUrl != null) {
            return audioUrl;
        }

        return vocabAudioService.synthesizeAndStoreAudio(word, ipa);
    }

    private String firstPresent(String requestValue, String resolvedValue) {
        String normalizedRequestValue = vocabValidationService.blankToNull(requestValue);
        if (normalizedRequestValue != null) {
            return normalizedRequestValue;
        }
        return vocabValidationService.blankToNull(resolvedValue);
    }
}
