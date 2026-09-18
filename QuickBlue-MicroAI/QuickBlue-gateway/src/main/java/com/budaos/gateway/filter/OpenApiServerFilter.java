package com.budaos.gateway.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * OpenAPI 服务器地址修改过滤器
 * <p>
 * 修改微服务返回的 OpenAPI 文档中的 servers 字段，将其从实际地址改为网关地址
 * </p>
 *
 * @author QuickBlue
 */
@Slf4j
@Component
public class OpenApiServerFilter implements GlobalFilter, Ordered {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 只处理 /v3/api-docs/* 路径的响应
        if (!path.matches("/.*/v3/api-docs/.*") && !path.matches("/.*/v3/api-docs$")) {
            return chain.filter(exchange);
        }

        // 从路径中提取服务名，例如 /QuickBlue-system/v3/api-docs/1-system -> QuickBlue-system
        String serviceName = extractServiceName(path);
        if (serviceName == null) {
            return chain.filter(exchange);
        }

        // 判断是否是 swagger-config 请求
        boolean isSwaggerConfig = path.endsWith("/v3/api-docs") || path.endsWith("/v3/api-docs/swagger-config");

        ServerHttpResponse originalResponse = exchange.getResponse();
        ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                if (body instanceof Flux) {
                    Flux<? extends DataBuffer> fluxBody = Flux.from(body);
                    return super.writeWith(fluxBody.buffer().map(dataBuffers -> {
                        // 合并所有 DataBuffer
                        DataBufferFactory bufferFactory = originalResponse.bufferFactory();
                        DataBuffer join = bufferFactory.join(dataBuffers);
                        byte[] content = new byte[join.readableByteCount()];
                        join.read(content);
                        DataBufferUtils.release(join);

                        String originalBody = new String(content, StandardCharsets.UTF_8);

                        try {
                            // 解析 JSON
                            JsonNode jsonNode = objectMapper.readTree(originalBody);

                            // 如果是 ObjectNode（对象），则修改响应
                            if (jsonNode.isObject()) {
                                ObjectNode objectNode = (ObjectNode) jsonNode;

                                if (isSwaggerConfig) {
                                    // swagger-config 请求：修改 urls 字段
                                    if (objectNode.has("urls")) {
                                        JsonNode urlsNode = objectNode.get("urls");
                                        if (urlsNode.isArray()) {
                                            ArrayNode urlsArray = (ArrayNode) urlsNode;
                                            ArrayNode newUrlsArray = objectMapper.createArrayNode();

                                            for (JsonNode urlNode : urlsArray) {
                                                if (urlNode.isObject()) {
                                                    ObjectNode urlObj = (ObjectNode) urlNode;
                                                    String originalUrl = urlObj.get("url").asText();
                                                    // 添加服务前缀
                                                    urlObj.put("url", "/" + serviceName + originalUrl);
                                                    newUrlsArray.add(urlObj);
                                                }
                                            }
                                            objectNode.set("urls", newUrlsArray);
                                            log.debug("修改 swagger-config 中的 urls 字段，添加服务前缀: /{}", serviceName);
                                        }
                                    }
                                } else {
                                    // API 文档请求：修改 servers 字段
                                    ArrayNode servers = objectMapper.createArrayNode();
                                    ObjectNode server = objectMapper.createObjectNode();
                                    server.put("url", "/" + serviceName);
                                    server.put("description", "Gateway API Server");
                                    servers.add(server);

                                    // 替换 servers 字段
                                    objectNode.set("servers", servers);

                                    log.debug("修改 OpenAPI 文档中的 servers 字段: {} -> /{}", serviceName, serviceName);
                                }

                                // 转换为 JSON 字符串
                                String modifiedBody = objectMapper.writeValueAsString(objectNode);

                                return bufferFactory.wrap(modifiedBody.getBytes(StandardCharsets.UTF_8));
                            } else {
                                // 如果不是对象，返回原始内容
                                return bufferFactory.wrap(originalBody.getBytes(StandardCharsets.UTF_8));
                            }
                        } catch (Exception e) {
                            log.error("修改 OpenAPI 文档失败: {}", e.getMessage());
                            return bufferFactory.wrap(originalBody.getBytes(StandardCharsets.UTF_8));
                        }
                    }));
                }
                return super.writeWith(body);
            }
        };

        return chain.filter(exchange.mutate().response(decoratedResponse).build());
    }

    /**
     * 从路径中提取服务名
     * 例如：/QuickBlue-system/v3/api-docs/1-system -> QuickBlue-system
     *
     * @param path 路径
     * @return 服务名
     */
    private String extractServiceName(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }

        String[] parts = path.split("/");
        if (parts.length >= 2 && parts[1].startsWith("QuickBlue-")) {
            return parts[1];
        }
        return null;
    }

    @Override
    public int getOrder() {
        // 在 NettyRoutingFilter 之后执行，以便修改响应体
        return Ordered.LOWEST_PRECEDENCE - 1;
    }
}
