package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.requestDTO.ReqCreateTestDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.responseDTO.ResTestDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.service.testing.TestService;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.util.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1/tests")
@RequiredArgsConstructor
public class TestController {
    private final TestService testService;

    @PostMapping
    @ApiMessage("Tạo bài test")
    public ResponseEntity<ResTestDTO> create(@RequestBody ReqCreateTestDTO request) {
        return ResponseEntity.ok(testService.create(request));
    }
}
