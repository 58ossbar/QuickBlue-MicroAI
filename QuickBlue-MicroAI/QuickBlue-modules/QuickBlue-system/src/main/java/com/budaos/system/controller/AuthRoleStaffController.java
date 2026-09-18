package com.budaos.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.system.domain.form.AuthRoleStaffQueryForm;
import com.budaos.system.domain.form.AuthRoleStaffUpdateForm;
import com.budaos.system.domain.vo.StaffVO;
import com.budaos.system.domain.vo.AuthRoleSelectedVO;
import com.budaos.system.service.AuthRoleStaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色员工关联控制器
 *
 * @author budaos
 */
@Tag(name = "角色员工管理", description = "角色员工关联管理相关接口")
@RestController
@RequestMapping("/role/employee")
@RequiredArgsConstructor
public class AuthRoleStaffController {

    private final AuthRoleStaffService roleEmployeeService;

    /**
     * 查询某个角色下的员工列表（分页）
     */
    @Operation(summary = "查询某个角色下的员工列表")
    @PostMapping("/queryEmployee")
    public ApiResult<PageResponse<StaffVO>> queryEmployee(@Valid @RequestBody AuthRoleStaffQueryForm queryForm) {
        return roleEmployeeService.queryEmployee(queryForm);
    }

    /**
     * 获取某个角色下的所有员工列表（无分页）
     */
    @Operation(summary = "获取某个角色下的所有员工列表")
    @GetMapping("/getAllEmployeeByRoleId/{roleId}")
    public ApiResult<List<StaffVO>> listAllEmployeeRoleId(@PathVariable Long roleId) {
        return ApiResult.ok(roleEmployeeService.getAllEmployeeByRoleId(roleId));
    }

    /**
     * 从角色成员列表中移除员工
     */
    @Operation(summary = "从角色成员列表中移除员工")
    @GetMapping("/removeEmployee")
    @SaCheckPermission("system:role:employee:delete")
    public ApiResult<String> removeEmployee(@RequestParam Long employeeId, @RequestParam Long roleId) {
        return roleEmployeeService.removeRoleEmployee(employeeId, roleId);
    }

    /**
     * 从角色成员列表中批量移除员工
     */
    @Operation(summary = "从角色成员列表中批量移除员工")
    @PostMapping("/batchRemoveRoleEmployee")
    @SaCheckPermission("system:role:employee:batch:delete")
    public ApiResult<String> batchRemoveEmployee(@Valid @RequestBody AuthRoleStaffUpdateForm updateForm) {
        return roleEmployeeService.batchRemoveRoleEmployee(updateForm);
    }

    /**
     * 角色成员列表中批量添加员工
     */
    @Operation(summary = "角色成员列表中批量添加员工")
    @PostMapping("/batchAddRoleEmployee")
    @SaCheckPermission("system:role:employee:add")
    public ApiResult<String> addEmployeeList(@Valid @RequestBody AuthRoleStaffUpdateForm addForm) {
        return roleEmployeeService.batchAddRoleEmployee(addForm);
    }

    /**
     * 获取员工所有选中的角色和所有角色
     */
    @Operation(summary = "获取员工所有选中的角色和所有角色")
    @GetMapping("/getRoles/{employeeId}")
    public ApiResult<List<AuthRoleSelectedVO>> getRoleByEmployeeId(@PathVariable Long employeeId) {
        return ApiResult.ok(roleEmployeeService.getRoleInfoListByEmployeeId(employeeId));
    }
}
