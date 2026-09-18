package com.budaos.support.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据库备份配置 VO
 *
 * @author budaos
 */
@Data
@Schema(description = "数据库备份配置VO")
public class DatabaseBackupConfigVO {

    @Schema(description = "配置ID")
    private Long configId;

    @Schema(description = "数据库主机")
    private String dbHost;

    @Schema(description = "数据库端口")
    private Integer dbPort;

    @Schema(description = "数据库名称")
    private String dbName;

    @Schema(description = "数据库用户名")
    private String dbUsername;

    @Schema(description = "数据库密码")
    private String dbPassword;

    @Schema(description = "备份文件存储路径")
    private String backupPath;

    @Schema(description = "mysqldump命令路径")
    private String mysqldumpPath;

    @Schema(description = "是否启用自动备份")
    private Boolean autoBackupEnabled;

    @Schema(description = "自动备份cron表达式")
    private String autoBackupCron;

    @Schema(description = "备份保留天数")
    private Integer retentionDays;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
