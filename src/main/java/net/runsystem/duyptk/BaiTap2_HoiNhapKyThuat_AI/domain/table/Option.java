package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "options", uniqueConstraints = @UniqueConstraint(columnNames = {"question_id", "option_order"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Option {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    @ToString.Exclude
    private Question question;

    @Column(name = "option_order", nullable = false)
    private Integer optionOrder;

    @Column(name = "option_content", nullable = false)
    private String optionContent;

    @Column(name = "is_correct", nullable = false)
    private boolean correct;

    @Column(name = "audio_url")
    private String audioUrl;
}
