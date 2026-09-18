package com.budaos.system.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 角色的数据范围更新表单
 *
 * @author budaos
 */
@Data
public class AuthRoleDataPermissionUpdateForm {

    @Schema(description = "角色ID")
    @NotNull(message = "角色ID不能为空")
    private Long roleId;

    @Schema(description = "设置信息")
    @Valid
    private List<RoleUpdateDataScopeListFormItem> dataScopeItemList;

    @Data
    public static class RoleUpdateDataScopeListFormItem {

        @Schema(description = "数据范围类型")
        @NotNull(message = "数据范围类型不能为空")
        private Integer dataScopeType;

        @Schema(description = "可见范围")
        @NotNull(message = "可见范围不能为空")
        private Integer viewType;
    }
}
