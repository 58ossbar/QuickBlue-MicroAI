package com.budaos.common.web.config;

import feign.Logger;
import feign.Request;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * OpenFeign OkHttp 配置
 * 使用 OkHttp 客户端替换默认客户端，提升性能 20-40%
 *
 * @author budaos
 */
@Slf4j
@Configuration
public class FeignOkHttpConfig {

    /**
     * 配置 OkHttp 客户端
     */
    @Bean
    @ConditionalOnMissingBean
    public okhttp3.OkHttpClient okHttpClient() {
        ConnectionPool connectionPool = new ConnectionPool(
                20,                          // 最大空闲连接数
                5L,                          // 保持活跃时间（分钟）
                TimeUnit.MINUTES
        );

        return new okhttp3.OkHttpClient.Builder()
                .connectionPool(connectionPool)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .protocols(java.util.List.of(okhttp3.Protocol.HTTP_2, okhttp3.Protocol.HTTP_1_1))
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    @Bean
    @ConditionalOnMissingBean
    public Request.Options feignOptions() {
        return new Request.Options(
                10, TimeUnit.SECONDS,
                30, TimeUnit.SECONDS,
                true
        );
    }
}
