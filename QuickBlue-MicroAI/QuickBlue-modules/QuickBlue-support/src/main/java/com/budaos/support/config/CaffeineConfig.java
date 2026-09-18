package com.budaos.support.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Caffeine 缓存配置
 * 当配置 spring.cache.type=caffeine 时启用
 *
 * @author budaos
 */
@Configuration
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "caffeine", matchIfMissing = false)
public class CaffeineConfig {

    /**
     * Caffeine 本地缓存管理器
     */
    @Bean
    public CaffeineCacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(com.github.benmanes.caffeine.cache.Caffeine.newBuilder()
                .initialCapacity(100)      // 初始容量
                .maximumSize(1000)        // 最大缓存数量
                .expireAfterWrite(Duration.ofHours(1))  // 写入后1小时过期
                .recordStats()            // 记录统计信息
        );
        return cacheManager;
    }
}
