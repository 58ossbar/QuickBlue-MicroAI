package com.budaos.gateway.filter;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 首页过滤器 - 处理根路径请求
 */
@Component
public class IndexWebFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        
        // 处理根路径请求
        if ("/".equals(path)) {
            try {
                ClassPathResource resource = new ClassPathResource("static/index.html");
                String content = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
                
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.OK);
                response.getHeaders().setContentType(MediaType.TEXT_HTML);
                
                DataBufferFactory bufferFactory = response.bufferFactory();
                DataBuffer buffer = bufferFactory.wrap(content.getBytes(StandardCharsets.UTF_8));
                
                return response.writeWith(Mono.just(buffer));
            } catch (IOException e) {
                return Mono.error(e);
            }
        }
        
        return chain.filter(exchange);
    }
}
