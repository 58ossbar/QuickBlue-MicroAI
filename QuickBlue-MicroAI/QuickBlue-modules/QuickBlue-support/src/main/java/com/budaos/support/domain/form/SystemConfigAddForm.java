package com.budaos.support.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 添加配置表单
 */
@Data
public class SystemConfigAddForm {

    @Schema(description = "参数key")
    @NotBlank(message = "参数key不能为空")
    @Length(max = 255, message = "参数key最多255个字符")
    private String configKey;

    @Schema(description = "参数的值")
    @NotBlank(message = "参数的值不能为空")
    @Length(max = 60000, message = "参数的值最多60000个字符")
    private String configValue;

    @Schema(description = "参数名称")
    @NotBlank(message = "参数名称不能为空")
    @Length(max = 255, message = "参数名称最多255个字符")
    private String configName;

    @Schema(description = "备注")
    @Length(max = 255, message = "备注最多255个字符")
    private String remark;
}
