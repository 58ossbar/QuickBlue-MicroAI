package com.budaos.support.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 数据字典新建表单
 *
 * @author budaos
 */
@Data
public class DictionaryAddForm {

    @Schema(description = "字典名字", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "字典名字 不能为空")
    private String dictName;

    @Schema(description = "字典编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "字典编码 不能为空")
    private String dictCode;

    @Schema(description = "字典备注")
    private String remark;

}
