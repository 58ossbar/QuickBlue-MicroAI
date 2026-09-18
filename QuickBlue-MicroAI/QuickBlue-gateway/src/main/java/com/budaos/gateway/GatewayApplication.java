package com.budaos.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 网关服务启动类
 *
 * @author budaos
 */
@SpringBootApplication(scanBasePackages = {"com.budaos"})
@EnableDiscoveryClient
@Slf4j
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
        log.info("========================================");
        log.info("  网关服务启动成功");
        log.info("");
        log.info("  📖 API 文档聚合中心：http://localhost:8080/");
        log.info("");
        log.info("  📖 各微服务 API 文档访问地址:");
        log.info("  系统服务: http://localhost:8080/QuickBlue-system/doc.html");
        log.info("  业务服务: http://localhost:8080/QuickBlue-business/doc.html");
        log.info("  支撑服务: http://localhost:8080/QuickBlue-support/doc.html");
        log.info("  AI服务:   http://localhost:8080/QuickBlue-ai/doc.html");
        log.info("");
        log.info("  🔧 API 文档服务接口:");
        log.info("  服务列表：http://localhost:8080/api-docs/services");
        log.info("  健康检查：http://localhost:8080/api-docs/health");
        log.info("  服务概览：http://localhost:8080/api/services/overview");
        log.info("========================================");
    }
}
