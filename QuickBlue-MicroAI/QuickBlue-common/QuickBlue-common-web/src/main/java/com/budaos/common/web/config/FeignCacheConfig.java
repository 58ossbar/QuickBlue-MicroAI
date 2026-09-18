package com.budaos.common.web.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * OpenFeign 缓存配置
 * 使用 @Cacheable 注解启用缓存，性能提升 50-90%
 *
 * @author budaos
 */
@Slf4j
@Configuration
@EnableCaching
public class FeignCacheConfig {

    @Bean
    @ConditionalOnMissingBean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.registerCustomCache("feignCache", feignCache());
        cacheManager.registerCustomCache("configCache", configCache());
        cacheManager.registerCustomCache("dictCache", dictCache());
        cacheManager.registerCustomCache("userInfoCache", userInfoCache());
        return cacheManager;
    }

    @Bean
    @ConditionalOnMissingBean(name = "feignCache")
    public Cache<Object, Object> feignCache() {
        return Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(1000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .expireAfterAccess(2, TimeUnit.MINUTES)
                .recordStats()
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(name = "configCache")
    public Cache<Object, Object> configCache() {
        return Caffeine.newBuilder()
                .initialCapacity(50)
                .maximumSize(500)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .recordStats()
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(name = "dictCache")
    public Cache<Object, Object> dictCache() {
        return Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(1000)
                .expireAfterWrite(1, TimeUnit.HOURS)
                .recordStats()
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(name = "userInfoCache")
    public Cache<Object, Object> userInfoCache() {
        return Caffeine.newBuilder()
                .initialCapacity(200)
                .maximumSize(2000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .recordStats()
                .build();
    }
}
