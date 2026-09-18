package com.budaos.gateway.controller;

import com.budaos.common.core.domain.ApiResult;
import com.budaos.gateway.domain.dto.DbConnectionDTO;
import com.budaos.gateway.domain.dto.InstallConfigDTO;
import com.budaos.gateway.domain.entity.EnvironmentConfigEntity;
import com.budaos.gateway.service.EnvironmentConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 环境配置管理控制器
 *
 * @author budaos
 */
@Slf4j
@RestController
@RequestMapping("/environment")
@RequiredArgsConstructor
@Tag(name = "环境配置管理接口", description = "环境配置管理相关接口")
public class EnvironmentConfigController {

    private final EnvironmentConfigService environmentConfigService;

    /**
     * 保存安装配置为环境
     */
    @Operation(summary = "保存安装配置为环境")
    @PostMapping("/save")
    public Mono<ApiResult<Long>> saveEnvironment(@RequestBody SaveEnvironmentRequest request) {
        return Mono.fromCallable(() -> {
            Long id = environmentConfigService.saveEnvironmentConfig(request.getEnvName(), request.getInstallConfig());
            return ApiResult.ok(id);
        });
    }

    /**
     * 获取所有环境配置
     */
    @Operation(summary = "获取所有环境配置")
    @GetMapping("/list")
    public Mono<ApiResult<List<EnvironmentConfigEntity>>> listEnvironments() {
        return Mono.fromCallable(() -> {
            List<EnvironmentConfigEntity> environments = environmentConfigService.listAllEnvironments();
            return ApiResult.ok(environments);
        });
    }

    /**
     * 获取当前激活的环境
     */
    @Operation(summary = "获取当前激活的环境")
    @GetMapping("/active")
    public Mono<ApiResult<EnvironmentConfigEntity>> getActiveEnvironment() {
        return Mono.fromCallable(() -> {
            EnvironmentConfigEntity activeEnv = environmentConfigService.getActiveEnvironment();
            return ApiResult.ok(activeEnv);
        });
    }

    /**
     * 切换环境
     */
    @Operation(summary = "切换环境")
    @PostMapping("/switch")
    public Mono<ApiResult<Void>> switchEnvironment(@RequestBody SwitchEnvironmentRequest request) {
        return Mono.fromCallable(() -> {
            environmentConfigService.switchEnvironment(request.getEnvName());
            return ApiResult.ok();
        });
    }

    /**
     * 删除环境配置
     */
    @Operation(summary = "删除环境配置")
    @DeleteMapping("/{envName}")
    public Mono<ApiResult<Void>> deleteEnvironment(@PathVariable String envName) {
        return Mono.fromCallable(() -> {
            environmentConfigService.deleteEnvironment(envName);
            return ApiResult.ok();
        });
    }

    /**
     * 测试环境数据库连接
     */
    @Operation(summary = "测试环境数据库连接")
    @GetMapping("/{envName}/test-db")
    public Mono<ApiResult<Boolean>> testEnvironmentDbConnection(@PathVariable String envName) {
        return Mono.fromCallable(() -> {
            Boolean result = environmentConfigService.testEnvironmentDbConnection(envName);
            return ApiResult.ok(result);
        });
    }

    /**
     * 导出环境配置
     */
    @Operation(summary = "导出环境配置")
    @GetMapping("/{envName}/export")
    public Mono<ApiResult<String>> exportEnvironmentConfig(@PathVariable String envName) {
        return Mono.fromCallable(() -> {
            String configJson = environmentConfigService.exportEnvironmentConfig(envName);
            return ApiResult.ok(configJson);
        });
    }

    /**
     * 导入环境配置
     */
    @Operation(summary = "导入环境配置")
    @PostMapping("/import")
    public Mono<ApiResult<Void>> importEnvironmentConfig(@RequestBody ImportEnvironmentRequest request) {
        return Mono.fromCallable(() -> {
            environmentConfigService.importEnvironmentConfig(request.getConfigJson(), request.getEnvName());
            return ApiResult.ok();
        });
    }

    /**
     * 更新环境的数据库连接信息
     */
    @Operation(summary = "更新环境的数据库连接信息")
    @PostMapping("/{envName}/update-db")
    public Mono<ApiResult<Void>> updateEnvironmentDb(
        @PathVariable String envName,
        @RequestBody DbConnectionDTO dbConnection) {
        return Mono.fromCallable(() -> {
            environmentConfigService.updateEnvironmentDb(envName, dbConnection);
            return ApiResult.ok();
        });
    }

    // ========== 请求DTO ==========

    /**
     * 保存环境请求
     */
    @lombok.Data
    public static class SaveEnvironmentRequest {
        private String envName;
        private InstallConfigDTO installConfig;
    }

    /**
     * 切换环境请求
     */
    @lombok.Data
    public static class SwitchEnvironmentRequest {
        private String envName;
    }

    /**
     * 导入环境请求
     */
    @lombok.Data
    public static class ImportEnvironmentRequest {
        private String envName;
        private String configJson;
    }
}
