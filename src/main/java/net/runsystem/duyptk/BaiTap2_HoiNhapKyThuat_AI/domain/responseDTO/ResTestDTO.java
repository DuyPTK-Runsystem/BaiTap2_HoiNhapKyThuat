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
public class ResTestDTO {
    private Long id;
    private Integer numberOfQuestion;
    private Integer timeInMinute;
    private Integer correctAnswerCount;
    private Integer incorrectAnswerCount;
    private Long remainingTimeInSeconds;
    private boolean finished;
    private List<ResQuestionDTO> questions;
}
