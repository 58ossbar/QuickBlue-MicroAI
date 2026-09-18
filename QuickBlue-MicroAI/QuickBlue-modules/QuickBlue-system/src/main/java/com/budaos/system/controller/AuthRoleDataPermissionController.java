package com.budaos.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.system.domain.form.AuthRoleDataPermissionUpdateForm;
import com.budaos.system.domain.vo.AuthRoleDataPermissionVO;
import com.budaos.system.service.AuthRoleDataPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色的数据权限配置控制器
 *
 * @author budaos
 */
@Tag(name = "角色数据权限管理", description = "角色数据权限配置相关接口")
@RestController
@RequestMapping("/role")
@RequiredArgsConstructor
public class AuthRoleDataPermissionController {

    private final AuthRoleDataPermissionService roleDataScopeService;

    /**
     * 获取某角色所设置的数据范围
     */
    @Operation(summary = "获取某角色所设置的数据范围")
    @GetMapping("/dataScope/getRoleDataScopeList/{roleId}")
    public ApiResult<List<AuthRoleDataPermissionVO>> dataScopeListByRole(@PathVariable Long roleId) {
        return roleDataScopeService.getRoleDataScopeList(roleId);
    }

    /**
     * 批量设置某角色数据范围
     */
    @Operation(summary = "批量设置某角色数据范围")
    @PostMapping("/dataScope/updateRoleDataScopeList")
    @SaCheckPermission("system:role:dataScope:update")
    public ApiResult<String> updateRoleDataScopeList(@RequestBody @Valid AuthRoleDataPermissionUpdateForm roleDataScopeUpdateForm) {
        return roleDataScopeService.updateRoleDataScopeList(roleDataScopeUpdateForm);
    }
}
