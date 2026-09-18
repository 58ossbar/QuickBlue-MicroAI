package com.budaos.system.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 缓存清理工具
 *
 * @author budaos
 */
@Slf4j
@Component
public class CacheCleanerUtil implements CommandLineRunner {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 启动时清除旧的缓存数据
     */
    @Override
    public void run(String... args) {
        try {
            // 清除所有与登录相关的缓存
            Set<String> keys = redisTemplate.keys("QuickBlue:login_*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("已清除 {} 个旧缓存数据", keys.size());
            }
        } catch (Exception e) {
            log.warn("清除旧缓存数据失败", e);
        }
    }

    /**
     * 手动清除所有缓存
     */
    public void clearAllCache() {
        try {
            Set<String> keys = redisTemplate.keys("QuickBlue:*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("已清除 {} 个缓存数据", keys.size());
            }
        } catch (Exception e) {
            log.error("清除缓存失败", e);
            throw new RuntimeException("清除缓存失败", e);
        }
    }
}
