package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.requestDTO.ReqCreateVocabDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.requestDTO.ReqUpdateVocabDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.responseDTO.ResVocabBulkImportDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.responseDTO.ResVocabDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.service.vocab.VocabBulkImportService;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.service.vocab.VocabService;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.util.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1/vocabs")
@RequiredArgsConstructor
public class VocabController {
    private final VocabService vocabService;
    private final VocabBulkImportService vocabBulkImportService;

    @PostMapping
    @ApiMessage("Tạo từ vựng")
    public ResponseEntity<Object> create(
            @RequestBody ReqCreateVocabDTO request,
            @RequestParam(required = false, name = "vocabSetId") Long vocabSetId) {
        if (vocabSetId != null) {
            return ResponseEntity.ok(vocabService.create(request, vocabSetId));
        }
        return ResponseEntity.ok(vocabService.create(request));
    }

    @PostMapping(value = "/bulk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiMessage("Import từ vựng từ file .xlsx")
    public ResponseEntity<ResVocabBulkImportDTO> bulkImport(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false, name = "vocabSetId") Long vocabSetId) {
        return ResponseEntity.ok(vocabBulkImportService.importFile(file, vocabSetId));
    }

    @GetMapping("/lookup")
    @ApiMessage("Lấy chi tiết từ vựng")
    public ResponseEntity<ResVocabDTO> get(@RequestParam(required = false, name = "id") Long id,
            @RequestParam(required = false, name = "word") String word) {
        return ResponseEntity.ok(vocabService.get(id, word));
    }

    @PatchMapping("/lookup")
    @ApiMessage("Cập nhật nghĩa từ vựng")
    public ResponseEntity<ResVocabDTO> update(
            @RequestParam(required = false, name = "id") Long id,
            @RequestParam(required = false, name = "word") String word,
            @RequestBody ReqUpdateVocabDTO request) {
        return ResponseEntity.ok(vocabService.update(id, word, request));
    }

    @GetMapping(value = "/audio/{fileName}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> getAudio(@PathVariable("fileName") String fileName) {
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("audio/mpeg"))
                .body(vocabService.readAudioFile(fileName));
    }
}
