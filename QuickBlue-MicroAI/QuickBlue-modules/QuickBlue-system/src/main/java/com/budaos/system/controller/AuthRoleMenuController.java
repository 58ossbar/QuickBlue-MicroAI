package com.budaos.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.system.domain.form.AuthRoleMenuUpdateForm;
import com.budaos.system.domain.vo.AuthRoleMenuTreeVO;
import com.budaos.system.service.AuthRoleMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 角色菜单控制器
 *
 * @author budaos
 */
@RestController
@Tag(name = "角色菜单管理")
@RequestMapping("/role/menu")
public class AuthRoleMenuController {

    @Resource
    private AuthRoleMenuService roleMenuService;

    @Operation(summary = "更新角色权限")
    @PostMapping("/update")
    @SaCheckPermission("system:role:menu:update")
    public ApiResult<String> updateRoleMenu(@Valid @RequestBody AuthRoleMenuUpdateForm updateDTO) {
        return roleMenuService.updateRoleMenu(updateDTO);
    }

    @Operation(summary = "获取角色关联菜单权限")
    @GetMapping("/getRoleSelectedMenu/{roleId}")
    public ApiResult<AuthRoleMenuTreeVO> getRoleSelectedMenu(@PathVariable Long roleId) {
        return roleMenuService.getRoleSelectedMenu(roleId);
    }

}
