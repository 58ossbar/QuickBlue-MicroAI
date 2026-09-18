package com.budaos.gateway.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 数据库连接DTO
 *
 * @author budaos
 */
@Data
@Schema(description = "数据库连接信息")
public class DbConnectionDTO {

    @Schema(description = "数据库类型", example = "postgresql")
    private String dbType;

    @Schema(description = "主机地址", example = "localhost")
    private String host;

    @Schema(description = "端口", example = "5432")
    private Integer port;

    @Schema(description = "管理员账号", example = "postgres")
    private String adminUser;

    @Schema(description = "管理员密码")
    private String adminPassword;

    @Schema(description = "数据库名称")
    private String databaseName;

    @Schema(description = "数据库用户名")
    private String databaseUser;

    @Schema(description = "数据库密码")
    private String databasePassword;
}
