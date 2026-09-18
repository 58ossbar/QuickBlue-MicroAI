package com.budaos.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.budaos.common.core.annotation.SkipAuth;
import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.CurrentUser;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.common.core.util.RequestContextUtil;
import com.budaos.common.excel.util.EasyExcelUtil;
import com.budaos.common.web.annotation.AuditLog;
import com.budaos.system.domain.entity.StaffEntity;
import com.budaos.system.domain.excel.StaffExcel;
import com.budaos.system.domain.form.*;
import com.budaos.system.domain.vo.StaffVO;
import com.budaos.system.service.StaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 员工控制器
 *
 * @author budaos
 */
@Slf4j
@Tag(name = "员工管理", description = "员工管理相关接口")
@AuditLog
@RestController
@RequestMapping("/employee")
public class StaffController {

    @Resource
    private StaffService employeeService;

    /**
     * 分页查询员工列表
     */
    @PostMapping("/query")
    @Operation(summary = "分页查询员工列表")
    public ApiResult<PageResponse<StaffVO>> query(@Valid @RequestBody StaffQueryForm queryForm) {
        return employeeService.queryEmployee(queryForm);
    }

    /**
     * 根据ID查询员工详情
     */
    @GetMapping("/{employeeId}")
    @Operation(summary = "根据ID查询员工详情")
    public ApiResult<StaffVO> getById(@PathVariable Long employeeId) {
        StaffVO employeeVO = employeeService.getEmployeeDetailVO(employeeId);
        return ApiResult.ok(employeeVO);
    }

    /**
     * 添加员工（返回初始密码）
     */
    @PostMapping("/add")
    @Operation(summary = "添加员工（返回初始密码）")
    @SaCheckPermission("system:employee:add")
    public ApiResult<String> add(@Valid @RequestBody StaffAddForm addForm) {
        return employeeService.addEmployee(addForm);
    }

    /**
     * 更新员工
     */
    @PostMapping("/update")
    @Operation(summary = "更新员工")
    @SaCheckPermission("system:employee:update")
    public ApiResult<String> update(@Valid @RequestBody StaffUpdateForm updateForm) {
        return employeeService.updateEmployee(updateForm);
    }

    /**
     * 更新个人中心信息
     */
    @PostMapping("/update/center")
    @Operation(summary = "更新个人中心信息")
    public ApiResult<String> updateCenter(@Valid @RequestBody StaffUpdateCenterForm updateCenterForm) {
        updateCenterForm.setEmployeeId(RequestContextUtil.getRequestUserId());
        return employeeService.updateCenter(updateCenterForm);
    }

    /**
     * 更新头像
     */
    @PostMapping("/update/avatar")
    @Operation(summary = "更新头像")
    public ApiResult<String> updateAvatar(@RequestBody StaffUpdateAvatarForm avatarForm) {
        avatarForm.setEmployeeId(RequestContextUtil.getRequestUserId());
        return employeeService.updateAvatar(avatarForm.getEmployeeId(), avatarForm.getAvatar());
    }

    /**
     * 更新员工禁用/启用状态
     */
    @GetMapping("/update/disabled/{employeeId}")
    @Operation(summary = "更新员工禁用/启用状态")
    @SaCheckPermission("system:employee:disabled")
    public ApiResult<String> updateDisabledFlag(@PathVariable Long employeeId) {
        return employeeService.updateDisableFlag(employeeId);
    }

    /**
     * 批量删除员工
     */
    @PostMapping("/update/batch/delete")
    @Operation(summary = "批量删除员工")
    @SaCheckPermission("system:employee:delete")
    public ApiResult<String> batchDelete(@RequestBody List<Long> employeeIdList) {
        return employeeService.batchDelete(employeeIdList);
    }

    /**
     * 批量调整员工部门
     */
    @PostMapping("/update/batch/department")
    @Operation(summary = "批量调整员工部门")
    @SaCheckPermission("system:employee:department:update")
    public ApiResult<String> batchUpdateDepartment(@RequestBody StaffBatchUpdateOrganizationForm batchForm) {
        return employeeService.batchUpdateDepartment(batchForm.getEmployeeIdList(), batchForm.getDepartmentId());
    }

    /**
     * 修改密码
     */
    @PostMapping("/update/password")
    @Operation(summary = "修改密码")
    public ApiResult<String> updatePassword(@Valid @RequestBody StaffUpdatePasswordForm updatePasswordForm) {
        CurrentUser requestUser = RequestContextUtil.getRequestUser();
        updatePasswordForm.setEmployeeId(requestUser.getUserId());
        return employeeService.updatePassword(requestUser, updatePasswordForm);
    }

    /**
     * 重置员工密码
     */
    @GetMapping("/update/password/reset/{employeeId}")
    @Operation(summary = "重置员工密码")
    @SaCheckPermission("system:employee:password:reset")
    public ApiResult<String> resetPassword(@PathVariable Long employeeId) {
        return employeeService.resetPassword(employeeId);
    }

    /**
     * 根据部门ID查询员工列表
     */
    @GetMapping("/getAllEmployeeByDepartmentId/{departmentId}")
    @Operation(summary = "根据部门ID查询员工列表")
    public ApiResult<List<StaffVO>> getAllEmployeeByDepartmentId(@PathVariable Long departmentId) {
        return employeeService.getAllEmployeeByDepartmentId(departmentId);
    }

    /**
     * 查询所有员工
     */
    @GetMapping("/queryAll")
    @Operation(summary = "查询所有员工")
    public ApiResult<List<StaffVO>> queryAll(@RequestParam(value = "disabledFlag", required = false) Boolean disabledFlag) {
        return employeeService.queryAllEmployee(disabledFlag);
    }

    /**
     * 查询当前登录用户信息
     */
    @GetMapping("/current")
    @Operation(summary = "查询当前登录用户信息")
    public ApiResult<StaffVO> getCurrentUser() {
        Long employeeId = RequestContextUtil.getRequestUserId();
        StaffVO employeeVO = employeeService.getEmployeeDetailVO(employeeId);
        return ApiResult.ok(employeeVO);
    }

    /**
     * 导出员工数据到Excel
     */
    @GetMapping("/export")
    @Operation(summary = "导出员工数据到Excel")
    @SaCheckPermission("system:employee:export")
    public void exportEmployee(HttpServletResponse response,
                               @RequestParam(value = "disabledFlag", required = false) Boolean disabledFlag) {
        employeeService.exportEmployee(response, disabledFlag);
    }

    /**
     * 下载员工导入模板
     */
    @GetMapping("/template")
    @Operation(summary = "下载员工导入模板")
    public void downloadImportTemplate(HttpServletResponse response) {
        employeeService.downloadImportTemplate(response);
    }

    /**
     * 从Excel导入员工数据
     */
    @PostMapping("/import")
    @Operation(summary = "从Excel导入员工数据")
    @SaCheckPermission("system:employee:import")
    public ApiResult<String> importEmployee(@RequestParam("file") MultipartFile file) {
        try {
            // 校验文件格式
            if (file == null || file.isEmpty()) {
                return ApiResult.userErrorParam("请选择要导入的文件");
            }
            if (!EasyExcelUtil.isExcelFile(file.getOriginalFilename())) {
                return ApiResult.userErrorParam("文件格式不正确，请上传Excel文件");
            }

            // 解析Excel数据
            List<StaffExcel> employeeExcelList = EasyExcelUtil.importExcel(file, StaffExcel.class);

            // 导入数据
            return employeeService.importEmployee(employeeExcelList);
        } catch (Exception e) {
            log.error("导入员工数据失败", e);
            return ApiResult.userErrorParam("导入失败：" + e.getMessage());
        }
    }
}

