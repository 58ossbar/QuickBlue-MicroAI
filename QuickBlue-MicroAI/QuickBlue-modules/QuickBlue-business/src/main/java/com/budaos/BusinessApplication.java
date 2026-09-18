package com.budaos;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 业务服务启动类
 *
 * @author budaos
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.budaos.api"})
@MapperScan(value = {"com.budaos.business.notice.dao", "com.budaos.business.region.dao"}, annotationClass = org.apache.ibatis.annotations.Mapper.class)
@Slf4j
public class BusinessApplication {

    public static void main(String[] args) {
        SpringApplication.run(BusinessApplication.class, args);
        log.info("========================================");
        log.info("  业务服务启动成功");
        log.info("  访问地址: http://localhost:8082");
        log.info("  API文档: http://localhost:8082/doc.html");
        log.info("========================================");
    }
}
