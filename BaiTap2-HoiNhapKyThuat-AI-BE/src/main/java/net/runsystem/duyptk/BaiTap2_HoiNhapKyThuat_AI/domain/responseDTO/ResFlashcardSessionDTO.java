package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.responseDTO;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResFlashcardSessionDTO {
    private List<Long> sourceItemIds;
    private Integer numberOfFlashcards;
    private List<ResFlashcardDTO> flashcards;
}
