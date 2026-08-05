package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.service.vocab;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.requestDTO.ReqCreateVocabDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.responseDTO.ResVocabBulkImportDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.responseDTO.ResVocabBulkImportItemDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.Vocab;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.service.organization.VocabSetMembershipService;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.util.error.ExternalServerException;

@Service
@RequiredArgsConstructor
public class VocabBulkImportService {
    private static final int DATA_START_ROW_INDEX = 2;
    private static final int WORD_COLUMN_INDEX = 1;
    private static final int IPA_COLUMN_INDEX = 2;
    private static final int MEANING_COLUMN_INDEX = 3;
    private static final String XLSX_EXTENSION = ".xlsx";

    private final VocabService vocabService;
    private final VocabSetMembershipService vocabSetMembershipService;
    private final DataFormatter dataFormatter = new DataFormatter();

    public ResVocabBulkImportDTO importFile(MultipartFile file) {
        return importFile(file, null);
    }

    public ResVocabBulkImportDTO importFile(MultipartFile file, Long vocabSetId) {
        validateFile(file);
        if (vocabSetId != null) {
            vocabSetMembershipService.validateVocabSetAccess(vocabSetId);
        }

        try (InputStream inputStream = file.getInputStream();
                Workbook workbook = new XSSFWorkbook(inputStream)) {
            return importSheet(workbook.getSheetAt(0), vocabSetId);
        } catch (IOException exception) {
            throw new IllegalArgumentException("Không thể đọc file import vocab", exception);
        }
    }

    private ResVocabBulkImportDTO importSheet(Sheet sheet, Long vocabSetId) {
        List<ResVocabBulkImportItemDTO> items = new ArrayList<>();
        Set<String> importedWords = new HashSet<>();

        for (int rowIndex = DATA_START_ROW_INDEX; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (isBlankRow(row)) {
                continue;
            }

            items.add(importRow(row, rowIndex + 1, importedWords, vocabSetId));
        }

        return toImportResult(items);
    }

    private ResVocabBulkImportItemDTO importRow(
            Row row,
            int rowNumber,
            Set<String> importedWords,
            Long vocabSetId) {
        String word = cellValue(row, WORD_COLUMN_INDEX);
        try {
            String normalizedWord = normalizedWordKey(word);
            if (normalizedWord != null && importedWords.contains(normalizedWord)) {
                return failureItem(rowNumber, word, "Từ vựng bị trùng trong file import");
            }
            Vocab vocab = vocabService.createEntity(ReqCreateVocabDTO.builder()
                    .word(word)
                    .ipa(cellValue(row, IPA_COLUMN_INDEX))
                    .meaning(cellValue(row, MEANING_COLUMN_INDEX))
                    .build());
            if (vocabSetId != null) {
                vocabSetMembershipService.addVocabToSet(vocabSetId, vocab);
            }
            importedWords.add(normalizedWord);
            return ResVocabBulkImportItemDTO.builder()
                    .rowNumber(rowNumber)
                    .word(word)
                    .success(true)
                    .vocab(vocabService.convertToDTO(vocab))
                    .build();
        } catch (DataAccessException | ExternalServerException
                | IllegalArgumentException | RestClientException exception) {
            return failureItem(rowNumber, word, exception.getMessage());
        }
    }

    private ResVocabBulkImportItemDTO failureItem(int rowNumber, String word, String error) {
        return ResVocabBulkImportItemDTO.builder()
                .rowNumber(rowNumber)
                .word(word)
                .success(false)
                .error(error)
                .build();
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File import không được để trống");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase(Locale.ROOT).endsWith(XLSX_EXTENSION)) {
            throw new IllegalArgumentException("File import phải có định dạng .xlsx");
        }
    }

    private ResVocabBulkImportDTO toImportResult(List<ResVocabBulkImportItemDTO> items) {
        int successCount = (int) items.stream()
                .filter(ResVocabBulkImportItemDTO::isSuccess)
                .count();
        return ResVocabBulkImportDTO.builder()
                .totalRows(items.size())
                .successCount(successCount)
                .failureCount(items.size() - successCount)
                .items(items)
                .build();
    }

    private boolean isBlankRow(Row row) {
        return row == null
                || (cellValue(row, WORD_COLUMN_INDEX) == null
                && cellValue(row, IPA_COLUMN_INDEX) == null
                && cellValue(row, MEANING_COLUMN_INDEX) == null);
    }

    private String cellValue(Row row, int cellIndex) {
        if (row == null) {
            return null;
        }

        Cell cell = row.getCell(cellIndex);
        if (cell == null) {
            return null;
        }

        String value = dataFormatter.formatCellValue(cell);
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String normalizedWordKey(String word) {
        if (word == null || word.isBlank()) {
            return null;
        }
        return word.trim().toLowerCase(Locale.ROOT);
    }
}
