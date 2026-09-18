package com.budaos.support.nacos.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Nacos命名空间VO
 *
 * @author budaos
 * @since 2026-02-24
 */
@Data
@Schema(description = "Nacos命名空间VO")
public class NacosNamespaceVO {

    @Schema(description = "命名空间ID")
    private String namespace;

    @Schema(description = "命名空间名称")
    private String namespaceShowName;

    @Schema(description = "配额")
    private Integer quota;

    @Schema(description = "配置数量")
    private Integer configCount;

    @Schema(description = "类型")
    private Integer type;
}
