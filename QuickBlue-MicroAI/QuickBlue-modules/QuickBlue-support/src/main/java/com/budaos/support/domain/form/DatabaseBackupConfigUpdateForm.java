package com.budaos.support.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 数据库备份配置更新表单
 *
 * @author budaos
 */
@Data
@Schema(description = "数据库备份配置更新表单")
public class DatabaseBackupConfigUpdateForm {

    @Schema(description = "配置ID")
    private Long configId;

    @Schema(description = "数据库主机")
    @NotBlank(message = "数据库主机不能为空")
    private String dbHost;

    @Schema(description = "数据库端口")
    @NotNull(message = "数据库端口不能为空")
    private Integer dbPort;

    @Schema(description = "数据库名称")
    @NotBlank(message = "数据库名称不能为空")
    private String dbName;

    @Schema(description = "数据库用户名")
    @NotBlank(message = "数据库用户名不能为空")
    private String dbUsername;

    @Schema(description = "数据库密码")
    @NotBlank(message = "数据库密码不能为空")
    private String dbPassword;

    @Schema(description = "备份文件存储路径")
    @NotBlank(message = "备份文件存储路径不能为空")
    private String backupPath;

    @Schema(description = "mysqldump命令路径(可选)")
    private String mysqldumpPath;

    @Schema(description = "是否启用自动备份")
    private Boolean autoBackupEnabled;

    @Schema(description = "自动备份cron表达式")
    private String autoBackupCron;

    @Schema(description = "备份保留天数")
    private Integer retentionDays;

    @Schema(description = "备注")
    private String remark;
}
