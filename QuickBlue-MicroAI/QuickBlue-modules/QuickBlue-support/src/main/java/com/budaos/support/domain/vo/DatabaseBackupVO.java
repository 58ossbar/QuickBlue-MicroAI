package com.budaos.support.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据库备份 VO
 *
 * @author budaos
 */
@Data
@Schema(description = "数据库备份VO")
public class DatabaseBackupVO {

    @Schema(description = "备份ID")
    private Long backupId;

    @Schema(description = "备份文件名")
    private String fileName;

    @Schema(description = "备份文件路径")
    private String filePath;

    @Schema(description = "备份文件大小")
    private Long fileSize;

    @Schema(description = "文件大小(格式化)")
    private String fileSizeDisplay;

    @Schema(description = "备份类型")
    private Integer backupType;

    @Schema(description = "备份类型名称")
    private String backupTypeName;

    @Schema(description = "备份状态")
    private Integer backupStatus;

    @Schema(description = "备份状态名称")
    private String backupStatusName;

    @Schema(description = "备份描述")
    private String remark;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "操作人")
    private String operator;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
