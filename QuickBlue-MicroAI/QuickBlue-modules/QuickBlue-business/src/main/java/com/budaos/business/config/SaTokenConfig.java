package com.budaos.business.config;

import cn.dev33.satoken.stp.StpUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Sa-Token 配置
 *
 * @author budaos
 */
@Configuration
@ConditionalOnProperty(
        prefix = "business.sa-token",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class SaTokenConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 手动创建 Sa-Token 登录校验拦截器
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                // 执行 Sa-Token 登录校验
                StpUtil.checkLogin();
                return true;
            }
        })
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/login",           // 登录接口
                        "/login/**",        // 登录相关接口
                        "/logout",          // 登出接口
                        "/captcha/**",      // 验证码接口
                        "/error",           // 错误页面
                        "/doc.html",        // Swagger 文档
                        "/doc-business.html", // Business 服务专用文档
                        "/swagger-ui.html", // Swagger UI
                        "/swagger-ui/**",   // Swagger UI 资源
                        "/swagger-resources/**",
                        "/v3/api-docs",           // OpenAPI 文档根路径
                        "/v3/api-docs/",          // OpenAPI 文档根路径带斜杠
                        "/v3/api-docs/**",        // OpenAPI 文档所有子路径
                        "/v3/api-docs-business/**", // Business 服务 OpenAPI 文档
                        "/v3/api-docs/swagger-config",
                        "/webjars/**",
                        "/favicon.ico",
                        "/actuator/**",    // 监控端点
                        "/health"          // 健康检查
                );
    }
}
