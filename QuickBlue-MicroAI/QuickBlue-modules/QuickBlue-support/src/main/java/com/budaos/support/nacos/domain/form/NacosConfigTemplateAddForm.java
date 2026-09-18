package com.budaos.support.nacos.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Nacos配置模板添加表单
 *
 * @author budaos
 * @since 2026-02-24
 */
@Data
@Schema(description = "Nacos配置模板添加表单")
public class NacosConfigTemplateAddForm {

    @Schema(description = "模板名称")
    @NotBlank(message = "模板名称不能为空")
    private String templateName;

    @Schema(description = "模板编码")
    @NotBlank(message = "模板编码不能为空")
    private String templateCode;

    @Schema(description = "配置ID模板")
    @NotBlank(message = "配置ID不能为空")
    private String dataId;

    @Schema(description = "配置分组模板")
    @NotBlank(message = "配置分组不能为空")
    private String groupId;

    @Schema(description = "配置内容模板")
    @NotBlank(message = "配置内容不能为空")
    private String content;

    @Schema(description = "配置类型: yaml/properties/text")
    private String type;

    @Schema(description = "模板描述")
    private String description;
}
