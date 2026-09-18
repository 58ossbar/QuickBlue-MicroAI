package com.budaos.support.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * 字典数据表更新表单
 *
 * @author budaos
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DictionaryDataUpdateForm extends DictionaryDataAddForm {

    @Schema(description = "字典数据id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "字典数据id 不能为空")
    private Long dictDataId;

    @Schema(description = "字典数据编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "字典数据编码 不能为空")
    private String dictCode;

}
