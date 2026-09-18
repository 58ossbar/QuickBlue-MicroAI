package com.budaos.gateway.filter;

import com.budaos.gateway.config.CustomGatewayProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 认证过滤器
 *
 * @author budaos
 */
@Slf4j
@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private final CustomGatewayProperties gatewayProperties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public AuthenticationFilter(CustomGatewayProperties gatewayProperties) {
        this.gatewayProperties = gatewayProperties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        // 添加详细日志
        log.info("AuthenticationFilter检查路径: {}, 白名单: {}", path, gatewayProperties.getWhitelist());

        // 检查是否在白名单中
        if (isWhitelistPath(path)) {
            log.info("路径 {} 在白名单中，跳过认证", path);
            return chain.filter(exchange);
        }

        // 检查请求头中是否有 Token
        String token = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (token == null || token.isEmpty()) {
            log.warn("请求路径 {} 缺少认证Token", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            exchange.getResponse().getHeaders().set("Content-Type", "application/json");
            String errorResponse = "{\"code\":401,\"message\":\"未授权，请先登录\",\"success\":false}";
            return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(errorResponse.getBytes()))
            );
        }

        // 这里可以添加 Token 验证逻辑
        // 例如：调用认证服务验证 Token 有效性
        // boolean isValid = validateToken(token);
        // if (!isValid) {
        //     exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        //     return exchange.getResponse().setComplete();
        // }

        // 将 Token 添加到请求头，传递给下游服务
        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-User-Token", token)
                .build();

        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    @Override
    public int getOrder() {
        return 100;
    }

    /**
     * 检查路径是否在白名单中
     */
    private boolean isWhitelistPath(String path) {
        boolean inWhitelist = gatewayProperties.getWhitelist().stream()
                .anyMatch(pattern -> {
                    boolean matches = pathMatcher.match(pattern, path);
                    if (log.isDebugEnabled()) {
                        log.debug("路径 {} 匹配白名单规则 {}: {}", path, pattern, matches);
                    }
                    return matches;
                });
        return inWhitelist;
    }
}
