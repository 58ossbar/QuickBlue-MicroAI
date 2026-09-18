package com.budaos.business.config;

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
 * Business Service - Swagger/OpenAPI 配置
 * <p>
 * 定义业务服务的 API 分组配置
 * </p>
 *
 * @author QuickBlue
 */
@Configuration
public class BusinessSwaggerConfig {

    @Value("${spring.application.name}")
    private String serviceName;

    /**
     * 配置业务服务的 OpenAPI 信息
     */
    @Bean
    public OpenAPI customOpenAPI() {
        Server gatewayServer = new Server();
        gatewayServer.setUrl("/QuickBlue-business");
        gatewayServer.setDescription("Gateway API Server");

        return new OpenAPI()
                .servers(List.of(gatewayServer))
                .info(new Info()
                        .title("业务管理服务 API 文档")
                        .description("通知、区域等业务功能")
                        .version("4.0.0")
                        .contact(new Contact()
                                .name("QuickBlue Team")
                                .email("support@budaos.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }

    /**
     * 业务管理 API 分组
     */
    @Bean
    @Order(0)
    public GroupedOpenApi businessApi() {
        return GroupedOpenApi.builder()
                .group("2-business")
                .packagesToScan("com.budaos.business")
                .pathsToMatch("/**")
                .build();
    }
}
