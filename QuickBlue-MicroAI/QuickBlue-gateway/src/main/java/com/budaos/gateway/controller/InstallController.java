package com.budaos.gateway.controller;

import com.budaos.common.core.domain.ApiResult;
import com.budaos.gateway.domain.dto.*;
import com.budaos.gateway.domain.vo.EnvironmentCheckVO;
import com.budaos.gateway.domain.vo.InstallProgressVO;
import com.budaos.gateway.service.InstallService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;

/**
 * 系统安装控制器
 *
 * @author budaos
 */
@Slf4j
@RestController
@RequestMapping("/install")
@RequiredArgsConstructor
@Tag(name = "系统安装接口", description = "系统安装相关接口")
public class InstallController {

    private final InstallService installService;

    /**
     * 检查环境
     */
    @Operation(summary = "检查环境")
    @GetMapping("/check-environment")
    public Mono<ApiResult<EnvironmentCheckVO>> checkEnvironment() {
        return Mono.fromCallable(() -> installService.checkEnvironment())
            .map(result -> ApiResult.ok(result))
            .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 测试数据库连接
     */
    @Operation(summary = "测试数据库连接")
    @PostMapping("/test-db-connection")
    public Mono<ApiResult<Void>> testDbConnection(@RequestBody DbConnectionDTO dto) {
        log.info("收到测试数据库连接请求: dbType={}, host={}, port={}, adminUser={}",
            dto.getDbType(), dto.getHost(), dto.getPort(), dto.getAdminUser());

        return Mono.fromRunnable(() -> installService.testDbConnection(dto))
            .subscribeOn(Schedulers.boundedElastic())
            .then(Mono.defer(() -> {
                ApiResult<Void> response = ApiResult.ok();
                log.info("测试数据库连接成功，返回响应: code={}, ok={}, msg={}",
                    response.getCode(), response.getOk(), response.getMsg());
                return Mono.just(response);
            }))
            .onErrorResume(e -> {
                log.error("测试数据库连接失败", e);
                ApiResult<Void> response = ApiResult.error(
                    com.budaos.common.core.code.SystemErrorCodes.DB_ERROR,
                    e.getMessage());
                log.info("返回错误响应: code={}, ok={}, msg={}",
                    response.getCode(), response.getOk(), response.getMsg());
                return Mono.just(response);
            });
    }

    /**
     * 测试Redis连接
     */
    @Operation(summary = "测试Redis连接")
    @PostMapping("/test-redis-connection")
    public Mono<ApiResult<Void>> testRedisConnection(@RequestBody RedisConnectionDTO dto) {
        return Mono.fromRunnable(() -> installService.testRedisConnection(dto))
            .subscribeOn(Schedulers.boundedElastic())
            .then(Mono.just(ApiResult.ok()));
    }

    /**
     * 测试Nacos连接
     */
    @Operation(summary = "测试Nacos连接")
    @PostMapping("/test-nacos-connection")
    public Mono<ApiResult<Map<String, String>>> testNacosConnection(@RequestBody NacosConnectionDTO dto) {
        return Mono.fromCallable(() -> installService.testNacosConnection(dto))
            .map(result -> ApiResult.ok(result))
            .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 测试云存储连接
     */
    @Operation(summary = "测试云存储连接")
    @PostMapping("/test-cloud-storage")
    public Mono<ApiResult<Void>> testCloudStorage(@RequestBody CloudStorageDTO dto) {
        return Mono.fromRunnable(() -> installService.testCloudStorage(dto))
            .subscribeOn(Schedulers.boundedElastic())
            .then(Mono.just(ApiResult.ok()));
    }

    /**
     * 检查端口占用
     */
    @Operation(summary = "检查端口占用")
    @GetMapping("/check-port")
    public Mono<ApiResult<Boolean>> checkPort(@RequestParam Integer port) {
        return Mono.fromCallable(() -> installService.checkPort(port))
            .map(available -> ApiResult.ok(available))
            .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 开始安装
     */
    @Operation(summary = "开始安装")
    @PostMapping("/start")
    public Mono<ApiResult<InstallProgressVO>> startInstall(@RequestBody InstallConfigDTO dto) {
        return Mono.fromCallable(() -> installService.startInstall(dto))
            .map(progress -> ApiResult.ok(progress))
            .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 获取安装进度
     */
    @Operation(summary = "获取安装进度")
    @GetMapping("/progress")
    public Mono<ApiResult<InstallProgressVO>> getInstallProgress() {
        return Mono.fromCallable(() -> installService.getInstallProgress())
            .map(progress -> ApiResult.ok(progress))
            .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 下载安装配置
     */
    @Operation(summary = "下载安装配置")
    @GetMapping("/download-config")
    public Mono<Void> downloadConfig(ServerWebExchange exchange) {
        return installService.downloadConfig(exchange);
    }

    /**
     * 检查系统是否已安装
     */
    @Operation(summary = "检查系统是否已安装")
    @GetMapping("/check-installed")
    public Mono<ApiResult<Boolean>> checkInstalled() {
        return Mono.fromCallable(() -> installService.isInstalled())
            .map(installed -> ApiResult.ok(installed))
            .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 获取系统信息
     */
    @Operation(summary = "获取系统信息")
    @GetMapping("/system-info")
    public Mono<ApiResult<Map<String, Object>>> getSystemInfo() {
        return Mono.fromCallable(() -> installService.getSystemInfo())
            .map(info -> ApiResult.ok(info))
            .subscribeOn(Schedulers.boundedElastic());
    }
}
