package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "vocabs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vocab {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vocab_id")
    private Long id;

    @NotBlank(message = "Từ vựng không được để trống")
    @Column(name = "word", nullable = false, unique = true)
    private String word;

    @Column(name = "meaning")
    private String meaning;

    @Column(name = "ipa")
    private String ipa;

    @Column(name = "audio_url")
    private String audioUrl;
}
