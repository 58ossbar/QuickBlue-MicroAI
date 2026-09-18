package com.budaos.support.nacos.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Nacos配置VO
 *
 * @author budaos
 * @since 2026-02-24
 */
@Data
@Schema(description = "Nacos配置VO")
public class NacosConfigVO {

    @Schema(description = "配置ID")
    private String dataId;

    @Schema(description = "配置分组")
    private String groupId;

    @Schema(description = "命名空间ID")
    private String tenantId;

    @Schema(description = "配置名称")
    private String configName;

    @Schema(description = "配置内容")
    private String content;

    @Schema(description = "配置类型(yaml/properties/text)")
    private String type;

    @Schema(description = "MD5值")
    private String md5;

    @Schema(description = "配置描述")
    private String desc;
}
