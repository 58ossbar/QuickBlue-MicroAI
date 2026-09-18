package com.budaos.common.swagger.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Knife4j / SpringDoc OpenAPI 基础配置类
 * <p>
 * 该配置类用于配置微服务的 API 文档生成基础信息，基于 SpringDoc OpenAPI 3.0 规范
 * 提供全局的 OpenAPI Bean，各业务模块可继承或覆盖此配置实现自定义分组
 * </p>
 * <p>
 * 使用说明:
 * <ul>
 *     <li>各业务模块应创建自己的 SpringDocConfig 类，定义专属的 API 分组</li>
 *     <li>通过 @Order 控制配置优先级，模块级配置优先于公共配置</li>
 *     <li>使用数字前缀 (如"1-chat","2-model") 控制分组显示顺序</li>
 * </ul>
 * </p>
 *
 * @author QuickBlue
 */
@Configuration
public class SpringDocConfig implements WebMvcConfigurer {

    @Value("${spring.application.name}")
    private String serviceName;

    /**
     * 配置 OpenAPI 基础信息
     * <p>
     * 配置 API 文档的标题、描述、版本、联系人等信息
     * 注意：此 Bean 已移除，由各业务模块自行定义
     * </p>
     *
     * @return OpenAPI 配置对象
     */
    // 已移除 customOpenAPI() Bean，由各模块自行定义 OpenAPI

    /**
     * 配置默认 API 分组 (不启用)
     * <p>
     * 注意：各业务模块应创建自己的 SpringDocConfig 定义专属 API 分组
     * 公共配置仅提供 OpenAPI 基础信息
     * </p>
     */
    // 已移除 defaultApi() Bean，由各模块自行定义分组

    /**
     * 添加静态资源处理器 - 处理 favicon.ico 等请求
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 允许访问 favicon.ico
        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/static/");
    }
}
