package com.budaos.system.config;

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
 * System Service - Swagger/OpenAPI 配置
 * <p>
 * 定义系统管理服务的 API 分组配置
 * </p>
 *
 * @author QuickBlue
 */
@Configuration
public class SystemSwaggerConfig {

    @Value("${spring.application.name}")
    private String serviceName;

    /**
     * 配置系统服务的 OpenAPI 信息
     */
    @Bean
    public OpenAPI customOpenAPI() {
        Server gatewayServer = new Server();
        gatewayServer.setUrl("/QuickBlue-system");
        gatewayServer.setDescription("Gateway API Server");

        return new OpenAPI()
                .servers(List.of(gatewayServer))
                .info(new Info()
                        .title("系统管理服务 API 文档")
                        .description("员工管理、角色权限、登录认证等系统功能")
                        .version("4.0.0")
                        .contact(new Contact()
                                .name("QuickBlue Team")
                                .email("support@budaos.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }

    /**
     * 系统管理 API 分组
     */
    @Bean
    @Order(0)
    public GroupedOpenApi systemApi() {
        return GroupedOpenApi.builder()
                .group("1-system")
                .packagesToScan("com.budaos.system.controller")
                .pathsToMatch("/**")
                .build();
    }
}
