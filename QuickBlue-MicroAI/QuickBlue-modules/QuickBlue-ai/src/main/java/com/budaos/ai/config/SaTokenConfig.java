package com.budaos.ai.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 配置
 *
 * @author budaos
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器，校验规则为 StpUtil.checkLogin() 登录校验
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                .order(100) // 设置拦截器顺序
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/login",           // 登录接口
                        "/login/**",        // 登录相关接口
                        "/logout",          // 登出接口
                        "/captcha/**",      // 验证码接口
                        "/error",           // 错误页面
                        "/doc.html",        // Swagger 文档
                        "/swagger-ui.html", // Swagger UI
                        "/swagger-ui/**",   // Swagger UI 资源
                        "/swagger-resources/**",
                        "/v3/api-docs",           // OpenAPI 文档根路径
                        "/v3/api-docs/",          // OpenAPI 文档根路径带斜杠
                        "/v3/api-docs/**",        // OpenAPI 文档所有子路径
                        "/v3/api-docs/swagger-config",
                        "/webjars/**",
                        "/favicon.ico",
                        "/actuator/**",    // 监控端点
                        "/health",          // 健康检查
                        "/ai/chat/**",      // AI聊天接口
                        "/ai/model/**",     // AI模型接口
                        "/ai/knowledge/**"  // AI知识库接口
                );
    }
}
