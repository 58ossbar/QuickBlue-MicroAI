package com.budaos.support.manager;

import com.budaos.support.domain.entity.SystemConfigEntity;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 配置缓存管理器
 */
public class ConfigManager {

    /**
     * 配置缓存
     */
    private final ConcurrentHashMap<String, SystemConfigEntity> CONFIG_CACHE = new ConcurrentHashMap<>();

    /**
     * 清空缓存
     */
    public void clear() {
        CONFIG_CACHE.clear();
    }

    /**
     * 获取配置
     */
    public SystemConfigEntity get(String key) {
        return CONFIG_CACHE.get(key);
    }

    /**
     * 放入配置
     */
    public void put(String key, SystemConfigEntity entity) {
        CONFIG_CACHE.put(key, entity);
    }

    /**
     * 获取缓存大小
     */
    public int size() {
        return CONFIG_CACHE.size();
    }
}
