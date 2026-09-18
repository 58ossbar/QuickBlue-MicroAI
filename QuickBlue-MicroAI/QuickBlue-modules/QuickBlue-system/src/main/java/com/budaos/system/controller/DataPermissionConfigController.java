package com.budaos.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.common.core.domain.AutoValidateList;
import com.budaos.common.web.annotation.AuditLog;
import com.budaos.system.domain.form.DataPermissionConfigAddForm;
import com.budaos.system.domain.form.DataPermissionConfigQueryForm;
import com.budaos.system.domain.form.DataPermissionConfigUpdateForm;
import com.budaos.system.domain.vo.DataPermissionConfigVO;
import com.budaos.system.domain.vo.DataPermissionViewTypeVO;
import com.budaos.system.service.DataPermissionConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据权限配置控制器
 *
 * @author budaos
 */
@Tag(name = "数据权限配置管理")
@RestController
@RequestMapping("/data-scope-config")
@AuditLog(module = "数据权限配置管理", description = "数据权限配置操作")
public class DataPermissionConfigController {

    @Resource
    private DataPermissionConfigService dataScopeConfigService;

    /**
     * 获取数据权限配置列表（启用状态）
     */
    @Operation(summary = "获取数据权限配置列表")
    @GetMapping("/list")
    public ApiResult<List<DataPermissionConfigVO>> list() {
        return dataScopeConfigService.dataScopeConfigList();
    }

    /**
     * 分页查询
     */
    @Operation(summary = "分页查询")
    @PostMapping("/queryPage")
    @SaCheckPermission("system:dataScopeConfig:query")
    public ApiResult<PageResponse<DataPermissionConfigVO>> queryPage(@RequestBody @Valid DataPermissionConfigQueryForm queryForm) {
        return dataScopeConfigService.queryPage(queryForm);
    }

    /**
     * 添加
     */
    @Operation(summary = "添加")
    @PostMapping("/add")
    @SaCheckPermission("system:dataScopeConfig:add")
    public ApiResult<String> add(@RequestBody @Valid DataPermissionConfigAddForm addForm) {
        return dataScopeConfigService.add(addForm);
    }

    /**
     * 更新
     */
    @Operation(summary = "更新")
    @PostMapping("/update")
    @SaCheckPermission("system:dataScopeConfig:update")
    public ApiResult<String> update(@RequestBody @Valid DataPermissionConfigUpdateForm updateForm) {
        return dataScopeConfigService.update(updateForm);
    }

    /**
     * 删除
     */
    @Operation(summary = "删除")
    @GetMapping("/delete/{configId}")
    @SaCheckPermission("system:dataScopeConfig:delete")
    public ApiResult<String> delete(@PathVariable Long configId) {
        return dataScopeConfigService.delete(configId);
    }

    /**
     * 批量删除
     */
    @Operation(summary = "批量删除")
    @PostMapping("/batchDelete")
    @SaCheckPermission("system:dataScopeConfig:delete")
    public ApiResult<String> batchDelete(@RequestBody AutoValidateList<Long> idList) {
        return dataScopeConfigService.batchDelete(idList);
    }

    /**
     * 更新状态（启用/禁用）
     */
    @Operation(summary = "更新状态")
    @GetMapping("/updateStatus/{configId}")
    @SaCheckPermission("system:dataScopeConfig:updateStatus")
    public ApiResult<String> updateStatus(@PathVariable Long configId) {
        return dataScopeConfigService.updateStatus(configId);
    }

    /**
     * 获取数据可见范围类型列表
     */
    @Operation(summary = "获取数据可见范围类型列表")
    @GetMapping("/view-type-list")
    public ApiResult<List<DataPermissionViewTypeVO>> viewTypeList() {
        return ApiResult.ok(dataScopeConfigService.getViewTypeList());
    }
}
