package com.budaos.gateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

/**
 * 网关健康检查控制器
 *
 * @author budaos
 */
@Slf4j
@RestController
@RequestMapping("/actuator")
public class GatewayHealthController {

    private final RouteLocator routeLocator;

    public GatewayHealthController(RouteLocator routeLocator) {
        this.routeLocator = routeLocator;
    }

    /**
     * 健康检查端点
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "QuickBlue-gateway");
        health.put("timestamp", System.currentTimeMillis());
        return health;
    }

    /**
     * 网关信息端点
     */
    @GetMapping("/gateway/info")
    public Map<String, Object> gatewayInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("service", "QuickBlue-gateway");
        info.put("version", "4.0.0");
        info.put("description", "QuickBlue API Gateway");
        info.put("timestamp", System.currentTimeMillis());

        // 获取路由数量
        Flux<Route> routes = routeLocator.getRoutes();
        info.put("routeCount", routes.count().block());

        return info;
    }

    /**
     * 路由列表端点
     */
    @GetMapping("/gateway/routes")
    public Flux<Route> routes() {
        return routeLocator.getRoutes()
                .map(route -> {
                    log.debug("路由: {} -> {}", route.getId(), route.getUri());
                    return route;
                });
    }
}
