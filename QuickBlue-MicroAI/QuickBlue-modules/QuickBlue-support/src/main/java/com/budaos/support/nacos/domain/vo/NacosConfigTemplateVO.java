package com.budaos.support.nacos.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Nacos配置模板VO
 *
 * @author budaos
 * @since 2026-02-24
 */
@Data
@Schema(description = "Nacos配置模板VO")
public class NacosConfigTemplateVO {

    @Schema(description = "模板ID")
    private Long templateId;

    @Schema(description = "模板编码")
    private String templateCode;

    @Schema(description = "模板名称")
    private String templateName;

    @Schema(description = "配置ID模板")
    private String dataIdTemplate;

    @Schema(description = "配置分组")
    private String groupId;

    @Schema(description = "配置内容模板")
    private String contentTemplate;

    @Schema(description = "配置类型(yaml/properties/text)")
    private String configType;

    @Schema(description = "模板描述")
    private String description;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "状态")
    private Boolean status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "创建人")
    private String createBy;

    @Schema(description = "更新人")
    private String updateBy;
}
