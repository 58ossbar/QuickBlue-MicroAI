package com.budaos;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 系统服务启动类
 *
 * @author budaos
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.budaos.api"})
@MapperScan(value = "com.budaos.system.dao", annotationClass = org.apache.ibatis.annotations.Mapper.class)
@Slf4j
public class SystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(SystemApplication.class, args);
        log.info("========================================");
        log.info("  系统服务启动成功");
        log.info("  服务地址: http://localhost:8081");
        log.info("  API文档: http://localhost:8081/doc.html");
        log.info("  OpenAPI: http://localhost:8081/v3/api-docs/default");
        log.info("========================================");
    }
}
