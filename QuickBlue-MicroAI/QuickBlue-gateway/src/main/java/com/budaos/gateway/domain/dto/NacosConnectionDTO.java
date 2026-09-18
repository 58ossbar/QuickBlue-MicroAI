package com.budaos.gateway.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Nacos连接配置DTO
 *
 * @author budaos
 */
@Data
@Schema(description = "Nacos连接配置")
public class NacosConnectionDTO {

    /**
     * Nacos服务器地址
     */
    @Schema(description = "Nacos服务器地址", example = "localhost:8848")
    private String serverAddr;

    /**
     * 命名空间
     */
    @Schema(description = "命名空间", example = "QuickBlue-dev")
    private String namespace;

    /**
     * 配置分组
     */
    @Schema(description = "配置分组", example = "QuickBlue_GROUP")
    private String group;

    /**
     * 用户名
     */
    @Schema(description = "用户名", example = "nacos")
    private String username;

    /**
     * 密码
     */
    @Schema(description = "密码", example = "nacos")
    private String password;
}
