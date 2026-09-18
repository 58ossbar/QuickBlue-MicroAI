package com.budaos.system.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 角色更新表单
 *
 * @author budaos
 */
@Data
@Schema(description = "角色更新表单")
public class AuthRoleUpdateForm {

    @Schema(description = "角色ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "角色ID不能为空")
    private Long roleId;

    @Schema(description = "角色名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(min = 1, max = 20, message = "角色名称长度1-20字符")
    private String roleName;

    @Schema(description = "角色编码")
    @Size(min = 1, max = 20, message = "角色编码长度1-20字符")
    private String roleCode;

    @Schema(description = "角色描述")
    @Size(max = 255, message = "角色描述最多255字符")
    private String remark;
}
