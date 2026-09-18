package com.budaos.system.controller;

import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.system.domain.entity.SysPermissionDataRule;
import com.budaos.system.service.SysPermissionDataRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据权限规则控制器
 *
 * @author QuickBlue
 */
@Tag(name = "数据权限规则管理", description = "数据权限规则管理接口")
@RestController
@RequestMapping("/sys/permission-data-rule")
public class SysPermissionDataRuleController {

    @Resource
    private SysPermissionDataRuleService permissionDataRuleService;

    @Operation(summary = "分页查询数据权限规则")
    @GetMapping("/page")
    public ApiResult<PageResponse<SysPermissionDataRule>> queryPage(
            @Parameter(description = "权限ID") @RequestParam(required = false) Long permissionId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResponse<SysPermissionDataRule> pageResult = permissionDataRuleService.queryPage(permissionId, pageNum, pageSize);
        return ApiResult.ok(pageResult);
    }

    @Operation(summary = "根据ID查询数据权限规则")
    @GetMapping("/{ruleId}")
    public ApiResult<SysPermissionDataRule> getById(@PathVariable Long ruleId) {
        SysPermissionDataRule rule = permissionDataRuleService.selectById(ruleId);
        return ApiResult.ok(rule);
    }

    @Operation(summary = "添加数据权限规则")
    @PostMapping("/add")
    public ApiResult<String> add(@RequestBody SysPermissionDataRule rule) {
        return permissionDataRuleService.add(rule);
    }

    @Operation(summary = "更新数据权限规则")
    @PostMapping("/update")
    public ApiResult<String> update(@RequestBody SysPermissionDataRule rule) {
        return permissionDataRuleService.update(rule);
    }

    @Operation(summary = "删除数据权限规则")
    @PostMapping("/delete/{ruleId}")
    public ApiResult<String> delete(@PathVariable Long ruleId) {
        return permissionDataRuleService.delete(ruleId);
    }

    @Operation(summary = "批量删除数据权限规则")
    @PostMapping("/batch-delete")
    public ApiResult<String> batchDelete(@RequestBody List<Long> ruleIds) {
        return permissionDataRuleService.batchDelete(ruleIds);
    }
}
