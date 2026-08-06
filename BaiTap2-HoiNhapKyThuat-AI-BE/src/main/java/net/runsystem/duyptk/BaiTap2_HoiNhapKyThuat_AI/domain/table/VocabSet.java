package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table;

import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "vocab_sets")
@PrimaryKeyJoinColumn(name = "vocab_set_id")
@DiscriminatorValue("VOCAB_SET")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class VocabSet extends Item {
    @NotBlank(message = "Tên tập từ vựng không được để trống")
    @Column(name = "vocab_set_name", nullable = false)
    private String vocabSetName;

    @Column(name = "vocab_set_descp")
    private String vocabSetDescription;

    @ManyToMany
    @JoinTable(
            name = "vocab_vocab_set",
            joinColumns = @JoinColumn(name = "vocab_set_id"),
            inverseJoinColumns = @JoinColumn(name = "vocab_id"))
    @Builder.Default
    private Set<Vocab> vocabs = new LinkedHashSet<>();
}
