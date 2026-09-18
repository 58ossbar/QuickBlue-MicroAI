package com.budaos.admin;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * QuickBlue 服务监控中心
 * 基于 Spring Boot Admin 实现微服务监控管理
 */
@EnableAdminServer
@EnableDiscoveryClient
@SpringBootApplication
public class QuickBlueAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuickBlueAdminApplication.class, args);
        System.out.println("==========================================");
        System.out.println("    QuickBlue Admin Server Started!");
        System.out.println("    Dashboard: http://localhost:9090");
        System.out.println("==========================================");
    }
}
