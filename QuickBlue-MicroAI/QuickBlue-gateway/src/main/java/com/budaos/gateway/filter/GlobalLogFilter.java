package com.budaos.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 全局日志过滤器
 *
 * @author budaos
 */
@Slf4j
@Component
public class GlobalLogFilter implements GlobalFilter, Ordered {

    private static final String START_TIME = "startTime";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // 记录请求开始时间
        exchange.getAttributes().put(START_TIME, System.currentTimeMillis());

        // 记录请求信息
        String requestId = generateRequestId();
        String method = request.getMethod().name();
        String path = request.getPath().value();
        String ip = getClientIp(request);
        String userAgent = request.getHeaders().getFirst("User-Agent");

        log.info("========================================");
        log.info("请求ID: {}", requestId);
        log.info("请求时间: {}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        log.info("请求方法: {}", method);
        log.info("请求路径: {}", path);
        log.info("客户端IP: {}", ip);
        log.info("User-Agent: {}", userAgent);
        log.info("========================================");

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            // 计算请求耗时
            Long startTime = exchange.getAttribute(START_TIME);
            if (startTime != null) {
                long executeTime = System.currentTimeMillis() - startTime;
                int statusCode = exchange.getResponse().getStatusCode() != null
                        ? exchange.getResponse().getStatusCode().value()
                        : 0;

                log.info("========================================");
                log.info("响应状态: {}", statusCode);
                log.info("请求耗时: {} ms", executeTime);
                log.info("请求ID: {}", requestId);
                log.info("========================================");

                // 慢请求警告
                if (executeTime > 3000) {
                    log.warn("【慢请求警告】请求路径: {}, 耗时: {} ms", path, executeTime);
                }
            }
        }));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    /**
     * 生成请求ID
     */
    private String generateRequestId() {
        return System.currentTimeMillis() + "-" + Thread.currentThread().getId();
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(ServerHttpRequest request) {
        // 检查 X-Forwarded-For 头
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        // 检查 X-Real-IP 头
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        // 获取远程地址
        return request.getRemoteAddress() != null
                ? request.getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
    }
}
