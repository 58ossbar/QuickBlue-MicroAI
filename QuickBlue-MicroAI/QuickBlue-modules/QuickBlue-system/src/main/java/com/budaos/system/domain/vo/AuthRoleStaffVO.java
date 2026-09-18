package com.budaos.system.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 角色员工关联VO
 *
 * @author budaos
 */
@Data
@Schema(description = "角色员工关联VO")
public class AuthRoleStaffVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "角色ID")
    private Long roleId;

    @Schema(description = "员工ID")
    private Long employeeId;

    @Schema(description = "角色名称")
    private String roleName;
}
