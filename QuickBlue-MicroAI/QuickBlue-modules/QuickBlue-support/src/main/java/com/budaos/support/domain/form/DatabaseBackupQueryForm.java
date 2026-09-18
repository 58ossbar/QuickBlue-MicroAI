package com.budaos.support.domain.form;

import com.budaos.common.core.domain.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 数据库备份查询表单
 *
 * @author budaos
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "数据库备份查询表单")
public class DatabaseBackupQueryForm extends PageQuery {

    @Schema(description = "备份文件名")
    private String fileName;

    @Schema(description = "备份类型(1-自动备份 2-手动备份)")
    private Integer backupType;

    @Schema(description = "备份状态(0-备份中 1-备份成功 2-备份失败)")
    private Integer backupStatus;

    @Schema(description = "操作人")
    private String operator;

    @Schema(description = "创建时间在此之前")
    private LocalDateTime createTimeBefore;
}
