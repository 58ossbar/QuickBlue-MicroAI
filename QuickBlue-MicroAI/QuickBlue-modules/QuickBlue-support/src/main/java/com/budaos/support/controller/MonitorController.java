package com.budaos.support.controller;

import com.budaos.common.core.domain.ApiResult;
import com.budaos.support.monitor.MonitorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 服务监控控制器
 *
 * @author budaos
 */
@Tag(name = "服务监控", description = "微服务运行状态与指标监控")
@RestController
@RequestMapping("/monitor")
public class MonitorController {

    @Resource
    private MonitorService monitorService;

    /**
     * 服务监控概览：所有注册服务的健康状态与 JVM 指标汇总
     */
    @Operation(summary = "服务监控概览")
    @GetMapping("/overview")
    public ApiResult<Map<String, Object>> overview() {
        return ApiResult.ok(monitorService.getOverview());
    }

    /**
     * 指定服务的详细指标（内存、线程、运行时长、QPS）
     */
    @Operation(summary = "服务详细指标")
    @GetMapping("/metrics")
    public ApiResult<Map<String, Object>> metrics(@RequestParam("serviceName") String serviceName) {
        return ApiResult.ok(monitorService.getMetrics(serviceName));
    }
}
