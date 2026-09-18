package com.budaos.gateway.domain.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 环境配置实体
 *
 * @author budaos
 */
@Data
public class EnvironmentConfigEntity {

    /**
     * 环境ID
     */
    private Long id;

    /**
     * 环境名称（dev/test/prod/custom）
     */
    private String envName;

    /**
     * 环境描述
     */
    private String description;

    /**
     * 是否为当前激活环境
     */
    private Boolean active;

    /**
     * 配置JSON
     */
    private String configJson;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
