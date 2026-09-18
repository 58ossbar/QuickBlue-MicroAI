package com.budaos.support.nacos.domain.form;

import com.budaos.common.core.domain.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Nacos配置模板查询表单
 *
 * @author budaos
 * @since 2026-02-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "Nacos配置模板查询表单")
public class NacosConfigTemplateQueryForm extends PageQuery {

    @Schema(description = "模板名称")
    private String templateName;

    @Schema(description = "模板编码")
    private String templateCode;

    @Schema(description = "配置类型")
    private String type;
}
