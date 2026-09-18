package com.budaos.system.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 菜单更新表单
 *
 * @author budaos
 */
@Data
@Schema(description = "菜单更新表单")
public class NavMenuUpdateForm extends NavMenuBaseForm {

    @Schema(description = "菜单ID", required = true)
    @NotNull(message = "菜单ID不能为空")
    private Long menuId;

    @Schema(description = "更新人ID")
    private Long updateUserId;
}
