package com.budaos.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.system.domain.form.AuthRoleAddForm;
import com.budaos.system.domain.form.AuthRoleUpdateForm;
import com.budaos.system.domain.vo.AuthRoleVO;
import com.budaos.system.service.AuthRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色控制器
 *
 * @author budaos
 */
@Tag(name = "角色管理", description = "角色管理相关接口")
@RestController
@RequestMapping("/role")
public class AuthRoleController {

    @Resource
    private AuthRoleService roleService;

    /**
     * 添加角色
     */
    @PostMapping("/add")
    @Operation(summary = "添加角色")
    @SaCheckPermission("system:role:add")
    public ApiResult<String> add(@Valid @RequestBody AuthRoleAddForm addForm) {
        return roleService.addRole(addForm);
    }

    /**
     * 删除角色
     */
    @GetMapping("/delete/{roleId}")
    @Operation(summary = "删除角色")
    @SaCheckPermission("system:role:delete")
    public ApiResult<String> delete(@PathVariable Long roleId) {
        return roleService.deleteRole(roleId);
    }

    /**
     * 更新角色
     */
    @PostMapping("/update")
    @Operation(summary = "更新角色")
    @SaCheckPermission("system:role:update")
    public ApiResult<String> update(@Valid @RequestBody AuthRoleUpdateForm updateForm) {
        return roleService.updateRole(updateForm);
    }

    /**
     * 更新角色菜单权限
     */
    @PostMapping("/update/menu")
    @Operation(summary = "更新角色菜单权限")
    @SaCheckPermission("system:role:menu:update")
    public ApiResult<String> updateMenu(@RequestParam Long roleId, @RequestBody List<Long> menuIdList) {
        return roleService.updateRoleMenu(roleId, menuIdList);
    }

    /**
     * 根据ID查询角色
     */
    @GetMapping("/get/{roleId}")
    @Operation(summary = "根据ID查询角色")
    public ApiResult<AuthRoleVO> getById(@PathVariable Long roleId) {
        return roleService.getRoleById(roleId);
    }

    /**
     * 查询所有角色
     */
    @GetMapping("/getAll")
    @Operation(summary = "查询所有角色")
    public ApiResult<List<AuthRoleVO>> getAll() {
        return roleService.getAllRole();
    }
}
