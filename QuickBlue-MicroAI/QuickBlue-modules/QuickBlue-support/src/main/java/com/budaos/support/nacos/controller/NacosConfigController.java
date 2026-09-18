package com.budaos.support.nacos.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.support.nacos.domain.form.ConfigAuditQueryForm;
import com.budaos.support.nacos.domain.form.NacosConfigForm;
import com.budaos.support.nacos.domain.vo.ConfigAuditVO;
import com.budaos.support.nacos.domain.vo.ConfigHistoryVO;
import com.budaos.support.nacos.domain.vo.NacosConfigVO;
import com.budaos.support.nacos.domain.vo.NacosNamespaceVO;
import com.budaos.support.nacos.service.NacosConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Nacos配置管理控制器
 *
 * @author budaos
 * @since 2026-02-24
 */
@Tag(name = "配置管理", description = "Nacos配置管理相关接口")
@RestController
@RequestMapping("/nacos-config")
@RequiredArgsConstructor
public class NacosConfigController {

    private final NacosConfigService nacosConfigService;

    /**
     * 获取命名空间列表
     */
    @Operation(summary = "获取命名空间列表")
    @GetMapping("/namespaces")
    @SaCheckPermission("system:nacos-config:query")
    public ApiResult<List<NacosNamespaceVO>> listNamespaces() {
        return nacosConfigService.listNamespaces();
    }

    /**
     * 获取配置列表
     */
    @Operation(summary = "获取配置列表")
    @GetMapping("/configs")
    @SaCheckPermission("system:nacos-config:query")
    public ApiResult<PageResponse<NacosConfigVO>> listConfigs(
            @Parameter(description = "命名空间ID") @RequestParam(required = false) String tenantId,
            @Parameter(description = "配置分组") @RequestParam(required = false) String groupId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNo,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {
        return nacosConfigService.listConfigs(tenantId, groupId, pageNo, pageSize);
    }

    /**
     * 获取配置详情
     */
    @Operation(summary = "获取配置详情")
    @GetMapping("/config")
    @SaCheckPermission("system:nacos-config:query")
    public ApiResult<NacosConfigVO> getConfig(
            @Parameter(description = "配置ID") @RequestParam String dataId,
            @Parameter(description = "配置分组") @RequestParam String groupId,
            @Parameter(description = "命名空间ID") @RequestParam(required = false) String tenantId) {
        return nacosConfigService.getConfig(dataId, groupId, tenantId);
    }

    /**
     * 发布配置
     */
    @Operation(summary = "发布配置")
    @PostMapping("/publish")
    @SaCheckPermission("system:nacos-config:edit")
    public ApiResult<String> publishConfig(@Valid @RequestBody NacosConfigForm form) {
        return nacosConfigService.publishConfig(form);
    }

    /**
     * 删除配置
     */
    @Operation(summary = "删除配置")
    @DeleteMapping("/config")
    @SaCheckPermission("system:nacos-config:delete")
    public ApiResult<String> deleteConfig(
            @Parameter(description = "配置ID") @RequestParam String dataId,
            @Parameter(description = "配置分组") @RequestParam String groupId,
            @Parameter(description = "命名空间ID") @RequestParam(required = false) String tenantId) {
        return nacosConfigService.deleteConfig(dataId, groupId, tenantId);
    }

    /**
     * 获取配置历史版本
     */
    @Operation(summary = "获取配置历史版本")
    @GetMapping("/history")
    @SaCheckPermission("system:nacos-config:query")
    public ApiResult<PageResponse<ConfigHistoryVO>> getConfigHistory(
            @Parameter(description = "配置ID") @RequestParam String dataId,
            @Parameter(description = "配置分组") @RequestParam String groupId,
            @Parameter(description = "命名空间ID") @RequestParam(required = false) String tenantId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNo,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {
        return nacosConfigService.getConfigHistory(dataId, groupId, tenantId, pageNo, pageSize);
    }

    /**
     * 获取配置历史版本详情
     */
    @Operation(summary = "获取配置历史版本详情")
    @GetMapping("/history/detail")
    @SaCheckPermission("system:nacos-config:query")
    public ApiResult<ConfigHistoryVO> getConfigHistoryDetail(
            @Parameter(description = "历史版本ID") @RequestParam String nid,
            @Parameter(description = "配置ID") @RequestParam String dataId,
            @Parameter(description = "配置分组") @RequestParam String groupId,
            @Parameter(description = "命名空间ID") @RequestParam(required = false) String tenantId) {
        return nacosConfigService.getConfigHistoryDetail(nid, dataId, groupId, tenantId);
    }

    /**
     * 回滚配置到指定版本
     */
    @Operation(summary = "回滚配置到指定版本")
    @PostMapping("/rollback")
    @SaCheckPermission("system:nacos-config:edit")
    public ApiResult<String> rollbackConfig(
            @Parameter(description = "历史版本ID") @RequestParam String nid,
            @Parameter(description = "配置ID") @RequestParam String dataId,
            @Parameter(description = "配置分组") @RequestParam String groupId,
            @Parameter(description = "命名空间ID") @RequestParam(required = false) String tenantId) {
        return nacosConfigService.rollbackConfig(nid, dataId, groupId, tenantId);
    }

    /**
     * 分页查询配置审计记录
     */
    @Operation(summary = "分页查询配置审计记录")
    @PostMapping("/audit/page")
    @SaCheckPermission("system:nacos-config:audit")
    public ApiResult<PageResponse<ConfigAuditVO>> queryAuditByPage(@RequestBody ConfigAuditQueryForm queryForm) {
        return nacosConfigService.queryAuditByPage(queryForm);
    }

    /**
     * 查询审计详情
     */
    @Operation(summary = "查询审计详情")
    @GetMapping("/audit/{auditId}")
    @SaCheckPermission("system:nacos-config:audit")
    public ApiResult<ConfigAuditVO> getAuditDetail(
            @Parameter(description = "审计ID") @PathVariable Long auditId) {
        return nacosConfigService.getAuditDetail(auditId);
    }

    /**
     * 导出配置（支持选择导出）
     */
    @Operation(summary = "导出配置")
    @PostMapping("/export")
    @SaCheckPermission("system:nacos-config:query")
    public ApiResult<List<NacosConfigVO>> exportConfigs(@RequestBody(required = false) List<String> dataIds,
            @Parameter(description = "命名空间ID") @RequestParam(required = false) String tenantId,
            @Parameter(description = "配置分组") @RequestParam(required = false) String groupId) {
        return nacosConfigService.exportConfigs(dataIds, tenantId, groupId);
    }

    /**
     * 导入配置
     */
    @Operation(summary = "导入配置")
    @PostMapping("/import")
    @SaCheckPermission("system:nacos-config:edit")
    public ApiResult<String> importConfigs(@RequestBody List<NacosConfigForm> configs) {
        return nacosConfigService.importConfigs(configs);
    }
}
