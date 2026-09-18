package com.budaos.support.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 字典数据表新建表单
 *
 * @author budaos
 */
@Data
public class DictionaryDataAddForm {

    @Schema(description = "字典id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "字典id 不能为空")
    private Long dictId;

    @Schema(description = "字典项值", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "字典项值 不能为空")
    private String dataValue;

    @Schema(description = "字典项显示名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "字典项显示名称 不能为空")
    private String dataLabel;

    @Schema(description = "父级编码（用于树形字典）")
    private String parentCode;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "排序（越大越靠前）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "排序（越大越靠前） 不能为空")
    private Integer sortOrder;

}
