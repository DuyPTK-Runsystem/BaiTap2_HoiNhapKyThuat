package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.service.vocab;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;

import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.requestDTO.ReqCreateVocabDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.responseDTO.ResVocabBulkImportDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.Vocab;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.service.organization.VocabSetMembershipService;

class VocabBulkImportServiceTests {
    private static final Long VOCAB_SET_ID = 12L;
    private static final String OPTION = "option";
    private static final String WORD = "word";
    private static final String CONTENT_TYPE_XLSX =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    @Test
    void importFileShouldContinueWhenRowFails() throws Exception {
        VocabService vocabService = Mockito.mock(VocabService.class);
        Mockito.when(vocabService.createEntity(ArgumentMatchers.argThat(request -> hasWord(request, OPTION))))
                .thenReturn(vocabEntity(OPTION));
        Mockito.when(vocabService.createEntity(ArgumentMatchers.argThat(request -> hasWord(request, WORD))))
                .thenThrow(new IllegalArgumentException("Từ vựng đã tồn tại"));
        VocabBulkImportService importService = new VocabBulkImportService(
                vocabService,
                Mockito.mock(VocabSetMembershipService.class));

        ResVocabBulkImportDTO result = importService.importFile(importFile());

        Assertions.assertThat(result)
                .extracting(
                        ResVocabBulkImportDTO::getTotalRows,
                        ResVocabBulkImportDTO::getSuccessCount,
                        ResVocabBulkImportDTO::getFailureCount)
                .containsExactly(2, 1, 1);
    }

    @Test
    void importFileShouldAddOnlySuccessfulRowsToVocabSet() throws Exception {
        VocabService vocabService = Mockito.mock(VocabService.class);
        VocabSetMembershipService membershipService = Mockito.mock(VocabSetMembershipService.class);
        List<String> events = new ArrayList<>();
        Mockito.doAnswer(invocation -> {
            events.add("validated");
            return null;
        }).when(membershipService).validateVocabSetAccess(VOCAB_SET_ID);
        Mockito.when(membershipService.addVocabToSet(ArgumentMatchers.eq(VOCAB_SET_ID),
                ArgumentMatchers.<Vocab>argThat(vocab -> OPTION.equals(vocab.getWord()))))
                .thenAnswer(invocation -> {
                    events.add("added:" + invocation.<Vocab>getArgument(1).getWord());
                    return null;
                });
        Mockito.when(vocabService.createEntity(ArgumentMatchers.argThat(request -> hasWord(request, OPTION))))
                .thenReturn(vocabEntity(OPTION));
        Mockito.when(vocabService.createEntity(ArgumentMatchers.argThat(request -> hasWord(request, WORD))))
                .thenThrow(new IllegalArgumentException("Từ vựng đã tồn tại"));
        VocabBulkImportService importService = new VocabBulkImportService(vocabService, membershipService);

        ResVocabBulkImportDTO result = importService.importFile(importFile(), VOCAB_SET_ID);

        Assertions.assertThat(List.of(
                result.getTotalRows(),
                result.getSuccessCount(),
                result.getFailureCount(),
                events))
                .containsExactly(2, 1, 1, List.of("validated", "added:" + OPTION));
    }

    private boolean hasWord(ReqCreateVocabDTO request, String word) {
        return request != null && word.equals(request.getWord());
    }

    private MockMultipartFile importFile() throws Exception {
        try (Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Vocabulary");
            writeHeader(sheet.createRow(1));
            writeData(sheet.createRow(2), OPTION, null, "lựa chọn");
            writeData(sheet.createRow(3), WORD, null, "từ");
            workbook.write(outputStream);
            return new MockMultipartFile(
                    "file",
                    "VocabImportTemplate.xlsx",
                    CONTENT_TYPE_XLSX,
                    outputStream.toByteArray());
        }
    }

    private void writeHeader(Row row) {
        row.createCell(0).setCellValue("STT");
        row.createCell(1).setCellValue("Từ vựng (word)");
        row.createCell(2).setCellValue("Phiên âm (có thể bỏ trống)");
        row.createCell(3).setCellValue("Dịch nghĩa");
    }

    private void writeData(Row row, String word, String ipa, String meaning) {
        row.createCell(1).setCellValue(word);
        if (ipa != null) {
            row.createCell(2).setCellValue(ipa);
        }
        row.createCell(3).setCellValue(meaning);
    }

    private Vocab vocabEntity(String word) {
        return Vocab.builder()
                .id(1L)
                .word(word)
                .meaning("meaning")
                .ipa("/ipa/")
                .audioUrl("/api/v1/vocabs/audio/" + word + ".mp3")
                .build();
    }
}
