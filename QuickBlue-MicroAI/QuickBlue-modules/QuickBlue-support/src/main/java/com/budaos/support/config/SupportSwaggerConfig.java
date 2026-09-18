package com.budaos.support.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.List;

/**
 * Support Service - Swagger/OpenAPI 配置
 * <p>
 * 定义支撑服务的 API 分组配置
 * </p>
 *
 * @author QuickBlue
 */
@Configuration
public class SupportSwaggerConfig {

    @Value("${spring.application.name}")
    private String serviceName;

    /**
     * 配置支撑服务的 OpenAPI 信息
     */
    @Bean
    public OpenAPI customOpenAPI() {
        Server gatewayServer = new Server();
        gatewayServer.setUrl("/QuickBlue-support");
        gatewayServer.setDescription("Gateway API Server");

        return new OpenAPI()
                .servers(List.of(gatewayServer))
                .info(new Info()
                        .title("支撑服务 API 文档")
                        .description("文件、字典、代码生成器等支撑功能")
                        .version("4.0.0")
                        .contact(new Contact()
                                .name("QuickBlue Team")
                                .email("support@budaos.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }

    /**
     * 支撑服务 API 分组
     */
    @Bean
    @Order(0)
    public GroupedOpenApi supportApi() {
        return GroupedOpenApi.builder()
                .group("3-support")
                .packagesToScan("com.budaos.support.controller")
                .pathsToMatch("/**")
                .build();
    }
}
