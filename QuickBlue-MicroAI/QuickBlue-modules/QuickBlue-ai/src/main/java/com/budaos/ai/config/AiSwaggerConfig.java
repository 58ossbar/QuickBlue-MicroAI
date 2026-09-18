package com.budaos.ai.config;

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
 * AI Service - Swagger/OpenAPI 配置
 * <p>
 * 定义 AI服务的 API 分组配置
 * </p>
 *
 * @author QuickBlue
 */
@Configuration
public class AiSwaggerConfig {

    @Value("${spring.application.name}")
    private String serviceName;

    /**
     * 配置 AI 服务的 OpenAPI 信息
     */
    @Bean
    public OpenAPI customOpenAPI() {
        Server gatewayServer = new Server();
        gatewayServer.setUrl("/QuickBlue-ai");
        gatewayServer.setDescription("Gateway API Server");

        return new OpenAPI()
                .servers(List.of(gatewayServer))
                .info(new Info()
                        .title("AI 服务 API 文档")
                        .description("聊天、知识库、模型管理等 AI 功能")
                        .version("4.0.0")
                        .contact(new Contact()
                                .name("QuickBlue Team")
                                .email("support@budaos.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }

    /**
     * AI服务 API 分组
     */
    @Bean
    @Order(0)
    public GroupedOpenApi aiApi() {
        return GroupedOpenApi.builder()
                .group("4-ai")
                .packagesToScan("com.budaos.ai")
                .pathsToMatch("/**")
                .build();
    }
}
