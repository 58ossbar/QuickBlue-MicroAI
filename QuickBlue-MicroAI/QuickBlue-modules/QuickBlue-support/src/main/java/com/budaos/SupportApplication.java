package com.budaos;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 支撑服务启动类
 *
 * @author budaos
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.budaos.api"})
@MapperScan(value = {"com.budaos.support.dao", "com.budaos.support.jobtask.dao", "com.budaos.support.nacos.dao"}, annotationClass = org.apache.ibatis.annotations.Mapper.class)
public class SupportApplication {

    public static void main(String[] args) {
        SpringApplication.run(SupportApplication.class, args);
        System.out.println("========================================");
        System.out.println("  支撑服务启动成功");
        System.out.println("  访问地址: http://localhost:8083");
        System.out.println("  API文档: http://localhost:8083/doc.html");
        System.out.println("========================================");
    }
}
