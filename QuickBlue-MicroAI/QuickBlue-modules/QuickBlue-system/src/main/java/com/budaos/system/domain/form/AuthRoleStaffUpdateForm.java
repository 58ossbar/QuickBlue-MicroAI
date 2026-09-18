package com.budaos.system.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

/**
 * 角色员工更新表单
 *
 * @author budaos
 */
@Data
@Schema(description = "角色员工更新表单")
public class AuthRoleStaffUpdateForm {

    @Schema(description = "角色ID", required = true)
    @NotNull(message = "角色ID不能为空")
    private Long roleId;

    @Schema(description = "员工ID集合", required = true)
    @NotEmpty(message = "员工ID不能为空")
    private Set<Long> employeeIdList;
}
