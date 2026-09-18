package com.budaos.support.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * reload (内存热加载、钩子等)
 */
@Data
public class ConfigRefreshForm {

    @Schema(description = "标签")
    @NotBlank(message = "标签不能为空")
    private String tag;

    @Schema(description = "状态标识")
    @NotBlank(message = "状态标识不能为空")
    private String identification;

    @Schema(description = "参数")
    private String args;

}
