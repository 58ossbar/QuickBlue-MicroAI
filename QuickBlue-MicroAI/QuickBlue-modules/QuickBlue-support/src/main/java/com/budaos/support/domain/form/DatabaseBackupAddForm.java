package com.budaos.support.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 手动备份表单
 *
 * @author budaos
 */
@Data
@Schema(description = "手动备份表单")
public class DatabaseBackupAddForm {

    @Schema(description = "备份描述")
    private String remark;
}
