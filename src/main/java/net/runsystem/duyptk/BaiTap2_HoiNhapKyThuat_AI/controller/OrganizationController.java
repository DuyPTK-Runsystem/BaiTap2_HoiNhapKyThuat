package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.requestDTO.ReqCreateFolderDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.requestDTO.ReqCreateVocabSetDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.responseDTO.ResItemDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.service.organization.OrganizationService;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.util.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class OrganizationController {
    private final OrganizationService organizationService;

    @PostMapping("/folders")
    @ApiMessage("Tạo thư mục")
    public ResponseEntity<ResItemDTO> createFolder(@RequestBody ReqCreateFolderDTO request) {
        return ResponseEntity.ok(organizationService.createFolder(request));
    }

    @PostMapping("/vocab-sets")
    @ApiMessage("Tạo tập từ vựng")
    public ResponseEntity<ResItemDTO> createVocabSet(@RequestBody ReqCreateVocabSetDTO request) {
        return ResponseEntity.ok(organizationService.createVocabSet(request));
    }
}
