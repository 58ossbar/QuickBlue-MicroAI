package com.budaos.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.system.domain.form.OrganizationAddForm;
import com.budaos.system.domain.form.OrganizationUpdateForm;
import com.budaos.system.domain.vo.OrganizationTreeVO;
import com.budaos.system.domain.vo.OrganizationVO;
import com.budaos.system.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门控制器
 *
 * @author budaos
 */
@Tag(name = "部门管理", description = "部门管理相关接口")
@RestController
@RequestMapping("/department")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService departmentService;

    /**
     * 查询部门树形列表（不带数据权限，使用缓存）
     * 适用于需要获取完整部门树的场景（如角色权限配置等）
     */
    @Operation(summary = "查询部门树形列表（不带数据权限）")
    @GetMapping("/treeList")
    public ApiResult<List<OrganizationTreeVO>> departmentTree() {
        return departmentService.departmentTree();
    }

    /**
     * 查询部门树形列表（带数据权限控制）
     * 根据用户的数据权限返回可见的部门树
     * 
     * 数据权限说明：
     * - ME（仅本人）：显示用户所属部门
     * - DEPARTMENT（本部门）：显示用户所属部门
     * - DEPARTMENT_AND_SUB（本部门及以下）：显示用户所属部门及所有子部门
     * - ALL（全部）：显示所有部门
     */
    @Operation(summary = "查询部门树形列表（带数据权限）")
    @GetMapping("/treeListWithDataScope")
    public ApiResult<List<OrganizationTreeVO>> departmentTreeWithDataScope() {
        return departmentService.departmentTreeWithDataScope();
    }

    /**
     * 添加部门
     */
    @Operation(summary = "添加部门")
    @PostMapping("/add")
    @SaCheckPermission("system:department:add")
    public ApiResult<String> addDepartment(@Valid @RequestBody OrganizationAddForm addForm) {
        return departmentService.addDepartment(addForm);
    }

    /**
     * 更新部门
     */
    @Operation(summary = "更新部门")
    @PostMapping("/update")
    @SaCheckPermission("system:department:update")
    public ApiResult<String> updateDepartment(@Valid @RequestBody OrganizationUpdateForm updateForm) {
        return departmentService.updateDepartment(updateForm);
    }

    /**
     * 删除部门
     */
    @Operation(summary = "删除部门")
    @GetMapping("/delete/{departmentId}")
    @SaCheckPermission("system:department:delete")
    public ApiResult<String> deleteDepartment(@PathVariable Long departmentId) {
        return departmentService.deleteDepartment(departmentId);
    }

    /**
     * 查询所有部门
     */
    @Operation(summary = "查询所有部门")
    @GetMapping("/listAll")
    public ApiResult<List<OrganizationVO>> listAllDepartment() {
        return ApiResult.ok(departmentService.listAllWithDataScope());
    }

    /**
     * 根据ID查询部门
     */
    @Operation(summary = "根据ID查询部门")
    @GetMapping("/get/{departmentId}")
    public ApiResult<OrganizationVO> getDepartmentById(@PathVariable Long departmentId) {
        return ApiResult.ok(departmentService.getDepartmentById(departmentId));
    }

    /**
     * 获取自身及所有下级部门的ID列表（Feign调用专用）
     */
    @Operation(summary = "获取自身及所有下级部门的ID列表")
    @GetMapping("/selfAndChildren/{departmentId}")
    public ApiResult<List<Long>> getSelfAndChildrenIdList(@PathVariable Long departmentId) {
        return ApiResult.ok(departmentService.selfAndChildrenIdList(departmentId));
    }
}
