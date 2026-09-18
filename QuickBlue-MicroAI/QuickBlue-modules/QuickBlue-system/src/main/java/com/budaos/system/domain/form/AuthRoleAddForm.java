package com.budaos.system.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 角色添加表单
 *
 * @author budaos
 */
@Data
@Schema(description = "角色添加表单")
public class AuthRoleAddForm {

    @Schema(description = "角色名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "角色名称不能为空")
    @Size(min = 1, max = 20, message = "角色名称长度1-20字符")
    private String roleName;

    @Schema(description = "角色编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "角色编码不能为空")
    @Size(min = 1, max = 20, message = "角色编码长度1-20字符")
    private String roleCode;

    @Schema(description = "角色描述")
    @Size(max = 255, message = "角色描述最多255字符")
    private String remark;
}
