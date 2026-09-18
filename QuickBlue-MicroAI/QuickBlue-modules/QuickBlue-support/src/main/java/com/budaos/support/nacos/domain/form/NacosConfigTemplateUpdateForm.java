package com.budaos.support.nacos.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Nacos配置模板更新表单
 *
 * @author budaos
 * @since 2026-02-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "Nacos配置模板更新表单")
public class NacosConfigTemplateUpdateForm extends NacosConfigTemplateAddForm {

    @Schema(description = "模板ID")
    @NotNull(message = "模板ID不能为空")
    private Long templateId;
}
