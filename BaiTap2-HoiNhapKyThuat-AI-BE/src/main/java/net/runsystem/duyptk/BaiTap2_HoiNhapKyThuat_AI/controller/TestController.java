package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.requestDTO.ReqCreateFlashcardDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.requestDTO.ReqCreateTestDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.requestDTO.ReqFinishTestDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.responseDTO.ResFlashcardSessionDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.responseDTO.ResTestDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.service.testing.FlashcardService;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.service.testing.TestResultService;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.service.testing.TestService;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.util.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TestController {
    private final TestService testService;
    private final TestResultService testResultService;
    private final FlashcardService flashcardService;

    @PostMapping("/tests")
    @ApiMessage("Tạo bài test")
    public ResponseEntity<ResTestDTO> create(@RequestBody ReqCreateTestDTO request) {
        return ResponseEntity.ok(testService.create(request));
    }

    @GetMapping("/tests/{testId}")
    @ApiMessage("Lấy bài test")
    public ResponseEntity<ResTestDTO> get(@PathVariable("testId") Long testId) {
        return ResponseEntity.ok(testService.get(testId));
    }

    @PostMapping("/tests/{testId}/finish")
    @ApiMessage("Kết thúc bài test")
    public ResponseEntity<ResTestDTO> finish(
            @PathVariable("testId") Long testId,
            @RequestBody ReqFinishTestDTO request) {
        return ResponseEntity.ok(testResultService.finish(testId, request));
    }

    @GetMapping("/tests/{testId}/result")
    @ApiMessage("Lấy kết quả bài test")
    public ResponseEntity<ResTestDTO> result(@PathVariable("testId") Long testId) {
        return ResponseEntity.ok(testService.result(testId));
    }

    @PostMapping("/flashcards")
    @ApiMessage("Tạo flashcard")
    public ResponseEntity<ResFlashcardSessionDTO> createFlashcards(@RequestBody ReqCreateFlashcardDTO request) {
        return ResponseEntity.ok(flashcardService.create(request));
    }
}
