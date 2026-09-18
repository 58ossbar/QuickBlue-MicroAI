package com.budaos.support.nacos.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Nacos配置表单
 *
 * @author budaos
 * @since 2026-02-24
 */
@Data
@Schema(description = "Nacos配置表单")
public class NacosConfigForm {

    @Schema(description = "配置ID")
    @NotBlank(message = "配置ID不能为空")
    private String dataId;

    @Schema(description = "配置分组")
    @NotBlank(message = "配置分组不能为空")
    private String groupId;

    @Schema(description = "命名空间ID")
    private String tenantId;

    @Schema(description = "配置内容")
    @NotBlank(message = "配置内容不能为空")
    private String content;

    @Schema(description = "配置类型: yaml/properties/text")
    private String type;

    @Schema(description = "配置名称(用于展示)")
    private String configName;

    @Schema(description = "备注")
    private String remark;
}
