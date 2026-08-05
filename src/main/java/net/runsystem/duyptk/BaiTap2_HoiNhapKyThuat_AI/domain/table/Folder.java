package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "folders")
@PrimaryKeyJoinColumn(name = "folder_id")
@DiscriminatorValue("FOLDER")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class Folder extends Item {
    @NotBlank(message = "Tên thư mục không được để trống")
    @Column(name = "folder_name", nullable = false)
    private String folderName;
}
