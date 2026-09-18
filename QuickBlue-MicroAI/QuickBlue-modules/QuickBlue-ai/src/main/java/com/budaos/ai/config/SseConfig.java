package com.budaos.ai.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * SSE配置
 */
@Slf4j
@Configuration
public class SseConfig implements WebMvcConfigurer {

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        // 设置SSE异步请求超时时间为30分钟
        configurer.setDefaultTimeout(30 * 60 * 1000);
        log.info("SSE异步请求超时时间已设置为30分钟");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 配置CORS，支持SSE跨域
        registry.addMapping("/ai/chat/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
        log.info("CORS配置已完成");
    }
}
