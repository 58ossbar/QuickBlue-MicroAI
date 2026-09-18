package com.budaos.gateway.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 网关首页控制器
 * <p>
 * 提供 API 文档聚合页面的访问入口
 * </p>
 *
 * @author QuickBlue
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping
public class GatewayIndexController {

    private static final Map<String, Integer> SERVICE_PORTS = new HashMap<>();

    static {
        SERVICE_PORTS.put("QuickBlue-system", 8081);
        SERVICE_PORTS.put("QuickBlue-business", 8082);
        SERVICE_PORTS.put("QuickBlue-support", 8083);
        SERVICE_PORTS.put("QuickBlue-ai", 8084);
    }

    private final DiscoveryClient discoveryClient;

    /**
     * 获取所有微服务的概览信息（用于前端展示）
     *
     * @return 服务概览信息
     */
    @GetMapping(value = "/api/services/overview", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> getServicesOverview() {
        Map<String, Object> result = new HashMap<>();

        // 获取所有注册的服务
        List<String> serviceNames = discoveryClient.getServices();

        // 过滤出微服务（排除网关）
        List<String> microServices = serviceNames.stream()
                .filter(name -> name.startsWith("QuickBlue-"))
                .filter(name -> !name.equals("QuickBlue-gateway"))
                .sorted()
                .collect(Collectors.toList());

        // 构建服务信息（直接返回实际服务端口地址）
        Map<String, Map<String, String>> services = new HashMap<>();
        for (String serviceName : microServices) {
            Map<String, String> serviceInfo = new HashMap<>();
            Integer port = SERVICE_PORTS.get(serviceName);

            if (port != null) {
                String baseUrl = "http://localhost:" + port;
                serviceInfo.put("displayName", getServiceDisplayName(serviceName));
                serviceInfo.put("apiDocsUrl", baseUrl + "/v3/api-docs");
                serviceInfo.put("docHtmlUrl", baseUrl + "/doc.html");
                serviceInfo.put("icon", getServiceIcon(serviceName));
                serviceInfo.put("description", getServiceDescription(serviceName));
                serviceInfo.put("port", String.valueOf(port));
                services.put(serviceName, serviceInfo);
            }
        }

        result.put("services", services);
        result.put("count", services.size());
        result.put("timestamp", System.currentTimeMillis());

        log.info("返回 {} 个微服务的概览信息", services.size());

        return Mono.just(result);
    }

    /**
     * 获取服务显示名称
     *
     * @param serviceName 服务名称
     * @return 显示名称
     */
    private String getServiceDisplayName(String serviceName) {
        return switch (serviceName) {
            case "QuickBlue-system" -> "系统服务";
            case "QuickBlue-business" -> "业务服务";
            case "QuickBlue-support" -> "支撑服务";
            case "QuickBlue-ai" -> "AI服务";
            default -> serviceName;
        };
    }

    /**
     * 获取服务图标 emoji
     *
     * @param serviceName 服务名称
     * @return 图标 emoji
     */
    private String getServiceIcon(String serviceName) {
        return switch (serviceName) {
            case "QuickBlue-system" -> "⚙️";
            case "QuickBlue-business" -> "💼";
            case "QuickBlue-support" -> "🔧";
            case "QuickBlue-ai" -> "🤖";
            default -> "📦";
        };
    }

    /**
     * 获取服务描述
     *
     * @param serviceName 服务名称
     * @return 服务描述
     */
    private String getServiceDescription(String serviceName) {
        return switch (serviceName) {
            case "QuickBlue-system" -> "系统管理、用户权限、角色配置等核心功能";
            case "QuickBlue-business" -> "业务流程、订单管理、数据处理等功能";
            case "QuickBlue-support" -> "文件服务、任务调度、公共支撑服务";
            case "QuickBlue-ai" -> "人工智能、机器学习、智能推荐等服务";
            default -> "微服务";
        };
    }
}
