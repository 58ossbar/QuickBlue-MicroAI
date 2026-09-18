package com.budaos.gateway.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Redis连接DTO
 *
 * @author budaos
 */
@Data
@Schema(description = "Redis连接信息")
public class RedisConnectionDTO {

    @Schema(description = "主机地址", example = "localhost")
    private String host;

    @Schema(description = "端口", example = "6379")
    private Integer port;

    @Schema(description = "密码")
    private String password;

    @Schema(description = "数据库编号", example = "1")
    private Integer database;

    @Schema(description = "超时时间(秒)", example = "10")
    private Integer timeout;
}
