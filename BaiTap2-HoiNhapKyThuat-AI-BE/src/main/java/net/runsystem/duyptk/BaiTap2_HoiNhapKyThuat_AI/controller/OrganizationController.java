package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.requestDTO.ReqBulkAddVocabToSetDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.requestDTO.ReqCreateFolderDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.requestDTO.ReqCreateVocabSetDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.responseDTO.ResItemDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.responseDTO.ResVocabSetBulkAddDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.responseDTO.ResVocabSetVocabDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.service.organization.OrganizationItemLookupService;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.service.organization.OrganizationService;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.service.organization.VocabSetMembershipService;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.util.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class OrganizationController {
    private final OrganizationService organizationService;
    private final OrganizationItemLookupService organizationItemLookupService;
    private final VocabSetMembershipService vocabSetMembershipService;

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

    @GetMapping("/items/children")
    @ApiMessage("Lấy danh sách item con")
    public ResponseEntity<List<ResItemDTO>> getChildren(
            @RequestParam(required = false, name = "parentId") Long parentId) {
        return ResponseEntity.ok(organizationService.getChildren(parentId));
    }

    @GetMapping("/items/search")
    @ApiMessage("Tìm kiếm item theo tên")
    public ResponseEntity<List<ResItemDTO>> searchItems(@RequestParam("name") String name) {
        return ResponseEntity.ok(organizationItemLookupService.searchItems(name));
    }

    @GetMapping("/items/by-path")
    @ApiMessage("Lấy item theo path")
    public ResponseEntity<ResItemDTO> getItemByPath(@RequestParam("path") String path) {
        return ResponseEntity.ok(organizationItemLookupService.getItemByPath(path));
    }

    @PostMapping("/vocab-sets/{vocabSetId}/vocabs/{vocabId}")
    @ApiMessage("Thêm từ vựng vào tập từ vựng")
    public ResponseEntity<ResVocabSetVocabDTO> addVocabToSet(
            @PathVariable("vocabSetId") Long vocabSetId,
            @PathVariable("vocabId") Long vocabId) {
        return ResponseEntity.ok(vocabSetMembershipService.addVocabToSet(vocabSetId, vocabId));
    }

    @PostMapping("/vocab-sets/{vocabSetId}/vocabs/bulk")
    @ApiMessage("Thêm nhiều từ vựng vào tập từ vựng")
    public ResponseEntity<ResVocabSetBulkAddDTO> bulkAddVocabsToSet(
            @PathVariable("vocabSetId") Long vocabSetId,
            @RequestBody ReqBulkAddVocabToSetDTO request) {
        return ResponseEntity.ok(vocabSetMembershipService.bulkAddVocabsToSet(vocabSetId, request));
    }
}
