package com.budaos.ai;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * QuickBlue AI 服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.budaos")
@MapperScan(basePackages = "com.budaos.ai.**.mapper")
@ComponentScan(basePackages = "com.budaos")
@Slf4j
public class AiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiApplication.class, args);
        log.info("========================================");
        log.info("   QuickBlue AI 服务启动成功!");
        log.info("  访问地址: http://localhost:8084");
        log.info("  API文档: http://localhost:8084/doc.html");
        log.info("========================================");
    }
}
