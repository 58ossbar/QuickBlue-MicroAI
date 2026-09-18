package com.budaos.support.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * 缓存预热监听器
 * 确保缓存管理页面能够显示所有预定义的缓存名称
 *
 * @author budaos
 */
@Slf4j
@Component
public class CacheWarmupListener implements CommandLineRunner {

    @Resource
    private RedisCacheManager redisCacheManager;

    /**
     * 预定义的缓存名称列表
     */
    private static final String[] PREDEFINED_CACHE_NAMES = {
            // 登录相关缓存
            "login_request_employee",
            "login_user_permission",

            // 部门相关缓存
            "department_list_cache",
            "department_path_cache",
            "department_self_children_cache",
            "department_tree_cache",

            // 区域相关缓存
            "region_tree_cache"
    };

    @Override
    public void run(String... args) {
        if (redisCacheManager == null) {
            log.info("RedisCacheManager 未配置，跳过缓存预热");
            return;
        }

        try {
            // 预热：创建空的缓存条目，确保缓存名称在CacheManager中注册
            int successCount = 0;
            for (String cacheName : PREDEFINED_CACHE_NAMES) {
                try {
                    // 尝试获取缓存，如果不存在会自动创建
                    var cache = redisCacheManager.getCache(cacheName);
                    if (cache != null) {
                        successCount++;
                        log.debug("预热缓存: {}", cacheName);
                    }
                } catch (Exception e) {
                    log.warn("预热缓存失败: {}, 错误: {}", cacheName, e.getMessage());
                }
            }

            log.info("缓存预热完成，已注册 {}/{} 个缓存", successCount, PREDEFINED_CACHE_NAMES.length);
        } catch (Exception e) {
            log.error("缓存预热失败", e);
        }
    }
}
