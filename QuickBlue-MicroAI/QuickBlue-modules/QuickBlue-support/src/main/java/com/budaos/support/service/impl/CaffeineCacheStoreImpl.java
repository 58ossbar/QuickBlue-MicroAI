package com.budaos.support.service.impl;

import com.budaos.support.service.CacheStore;
import com.github.benmanes.caffeine.cache.Cache;
import com.google.common.collect.Lists;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * caffeine 缓存实现
 * 只有在 CaffeineCacheManager 存在时才生效
 */
@Service
@ConditionalOnBean(CaffeineCacheManager.class)
public class CaffeineCacheStoreImpl implements CacheStore {

    /** 内容摘要最大长度，超出部分省略 */
    private static final int MAX_VALUE_LENGTH = 500;

    @Resource
    private CaffeineCacheManager caffeineCacheManager;

    /**
     * 获取所有缓存名称
     */
    @Override
    public List<String> cacheNames() {
        return Lists.newArrayList(caffeineCacheManager.getCacheNames());
    }

    /**
     * 某个缓存下的所有 key
     */
    @Override
    public List<String> cacheKey(String cacheName) {
        CaffeineCache cache = (CaffeineCache) caffeineCacheManager.getCache(cacheName);
        if (cache == null) {
            return Lists.newArrayList();
        }
        Cache<Object, Object> nativeCache = (Cache<Object, Object>) cache.getNativeCache();
        Set<Object> cacheKey = nativeCache.asMap().keySet();
        return cacheKey.stream().map(e -> e.toString()).collect(Collectors.toList());
    }

    /**
     * 移除某个缓存
     */
    @Override
    public void removeCache(String cacheName) {
        CaffeineCache cache = (CaffeineCache) caffeineCacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }

    /**
     * 获取所有缓存信息（名称 + key 数量）
     */
    @Override
    public List<CacheInfo> cacheList() {
        Collection<String> names = caffeineCacheManager.getCacheNames();
        if (names == null || names.isEmpty()) {
            return Lists.newArrayList();
        }
        return names.stream()
            .map(name -> {
                List<String> keys = cacheKey(name);
                return new CacheInfo(name, keys.size());
            })
            .collect(Collectors.toList());
    }

    /**
     * 获取某个缓存下所有 key 的详情
     */
    @Override
    public List<CacheKeyDetail> cacheKeyDetails(String cacheName) {
        CaffeineCache cache = (CaffeineCache) caffeineCacheManager.getCache(cacheName);
        if (cache == null) {
            return Lists.newArrayList();
        }
        Cache<Object, Object> nativeCache = (Cache<Object, Object>) cache.getNativeCache();

        return nativeCache.asMap().entrySet().stream()
            .map(entry -> {
                Object key = entry.getKey();
                Object value = entry.getValue();
                String keyStr = key == null ? "" : key.toString();
                Long ttl = resolveTtl(nativeCache, key);
                return new CacheKeyDetail(keyStr, "OBJECT", ttl, truncate(String.valueOf(value)));
            })
            .collect(Collectors.toList());
    }

    /**
     * 解析单个 key 的剩余过期时间（秒），无法获取或永不过期返回 -1
     */
    private Long resolveTtl(Cache<Object, Object> nativeCache, Object key) {
        try {
            com.github.benmanes.caffeine.cache.Policy<Object, Object> policy = nativeCache.policy();
            Optional<OptionalLong> accessAge = policy.expireAfterAccess().map(e -> e.ageOf(key, TimeUnit.SECONDS));
            if (accessAge.isPresent() && accessAge.get().isPresent()) {
                return accessAge.get().getAsLong();
            }
            Optional<OptionalLong> writeAge = policy.expireAfterWrite().map(e -> e.ageOf(key, TimeUnit.SECONDS));
            if (writeAge.isPresent() && writeAge.get().isPresent()) {
                return writeAge.get().getAsLong();
            }
            return -1L;
        } catch (Exception e) {
            return -1L;
        }
    }

    /**
     * 移除某个缓存下的单个 key
     */
    @Override
    public void removeCacheKey(String cacheName, String key) {
        CaffeineCache cache = (CaffeineCache) caffeineCacheManager.getCache(cacheName);
        if (cache == null) {
            return;
        }
        Cache<Object, Object> nativeCache = (Cache<Object, Object>) cache.getNativeCache();
        nativeCache.asMap().remove(key);
    }

    /**
     * 截断超长内容
     */
    private String truncate(String value) {
        if (value == null) {
            return "";
        }
        if (value.length() > MAX_VALUE_LENGTH) {
            return value.substring(0, MAX_VALUE_LENGTH) + "...";
        }
        return value;
    }
}
