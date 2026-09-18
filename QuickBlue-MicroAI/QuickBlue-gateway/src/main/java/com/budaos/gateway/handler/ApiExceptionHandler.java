package com.budaos.gateway.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 *
 * @author budaos
 */
@Slf4j
@Order(-1)
@Component
public class ApiExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        // 设置响应头
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // 构建错误响应
        Map<String, Object> result = new HashMap<>();
        int statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
        String message = "服务器内部错误";

        if (ex instanceof ResponseStatusException) {
            ResponseStatusException rse = (ResponseStatusException) ex;
            statusCode = rse.getStatusCode().value();
            message = rse.getReason();
        } else {
            log.error("网关异常", ex);
        }

        result.put("code", statusCode);
        result.put("message", message);
        result.put("success", false);
        result.put("timestamp", System.currentTimeMillis());

        response.setStatusCode(HttpStatus.valueOf(statusCode));

        DataBuffer buffer = null;
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(result);
            buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("JSON序列化失败", e);
            return Mono.error(ex);
        } finally {
            if (buffer != null) {
                log.info("响应状态: {}, 响应内容: {}", statusCode, result);
            }
        }
    }
}
