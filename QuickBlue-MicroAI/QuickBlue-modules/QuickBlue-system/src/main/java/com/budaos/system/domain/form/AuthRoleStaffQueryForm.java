package com.budaos.system.domain.form;

import com.budaos.common.core.domain.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色员工查询表单
 *
 * @author budaos
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "角色员工查询表单")
public class AuthRoleStaffQueryForm extends PageQuery {

    @Schema(description = "关键字")
    private String keywords;

    @Schema(description = "角色ID")
    private String roleId;
}
