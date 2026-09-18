package com.budaos.support.service.impl;

import cn.hutool.core.util.StrUtil;
import com.budaos.support.service.CacheStore;
import com.google.common.collect.Lists;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * redis 缓存实现
 */
@Service
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
public class RedisCacheStoreImpl implements CacheStore {

    /** Redis key 前缀 */
    private static final String KEY_PREFIX = "QuickBlue:";

    /** 内容摘要最大长度，超出部分省略 */
    private static final int MAX_VALUE_LENGTH = 500;

    @Resource
    private RedisCacheManager redisCacheManager;

    @Resource
    private RedisConnectionFactory redisConnectionFactory;

    /**
     * 获取所有缓存名称
     */
    @Override
    public List<String> cacheNames() {
        // 首先获取CacheManager中已注册的缓存名称
        List<String> cacheNames = Lists.newArrayList(redisCacheManager.getCacheNames());

        // 如果没有缓存名称，则从Redis中扫描所有符合 QuickBlue: 前缀的key
        if (cacheNames.isEmpty()) {
            RedisConnection connection = redisConnectionFactory.getConnection();
            Set<byte[]> keys = connection.keyCommands().keys((KEY_PREFIX + "*").getBytes());

            if (keys != null && !keys.isEmpty()) {
                // 提取缓存名称（QuickBlue:cache_name:key 中的 cache_name）
                cacheNames = keys.stream()
                    .map(key -> {
                        String redisKey = StrUtil.str(key, "utf-8");
                        // 提取 QuickBlue: 后面的部分，再取第一个冒号前的部分作为缓存名
                        if (redisKey.startsWith(KEY_PREFIX)) {
                            String afterPrefix = redisKey.substring(KEY_PREFIX.length());
                            int colonIndex = afterPrefix.indexOf(":");
                            if (colonIndex > 0) {
                                return afterPrefix.substring(0, colonIndex);
                            }
                        }
                        return null;
                    })
                    .filter(name -> name != null && !name.isEmpty())
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
            }
            connection.close();
        }

        return cacheNames;
    }

    /**
     * 某个缓存下的所有 key
     */
    @Override
    public List<String> cacheKey(String cacheName) {
        // 尝试通过CacheManager获取缓存
        RedisCache cache = (RedisCache) redisCacheManager.getCache(cacheName);

        // 构建Redis key模式
        String keyPattern = KEY_PREFIX + cacheName + ":*";

        // 获取 Redis 连接
        RedisConnection connection = redisConnectionFactory.getConnection();
        Set<byte[]> keys = connection.keyCommands().keys(keyPattern.getBytes());

        if (keys != null) {
            List<String> result = keys.stream().map(key -> {
                String redisKey = StrUtil.str(key, "utf-8");
                // 从 Redis 键中提取出最后一个冒号后面的字符串作为真正的键
                return redisKey.substring(redisKey.lastIndexOf(":") + 1);
            }).collect(Collectors.toList());
            connection.close();
            return result;
        }
        connection.close();
        return Lists.newArrayList();
    }

    /**
     * 移除某个缓存
     */
    @Override
    public void removeCache(String cacheName) {
        // 首先尝试通过CacheManager获取缓存
        RedisCache cache = (RedisCache) redisCacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            return;
        }

        // 如果CacheManager中没有该缓存，则直接通过Redis连接删除
        RedisConnection connection = redisConnectionFactory.getConnection();
        Set<byte[]> keys = connection.keyCommands().keys((KEY_PREFIX + cacheName + ":*").getBytes());
        if (keys != null && !keys.isEmpty()) {
            connection.keyCommands().del(keys.toArray(new byte[0][]));
        }
        connection.close();
    }

    /**
     * 获取所有缓存信息（名称 + key 数量）
     */
    @Override
    public List<CacheInfo> cacheList() {
        List<String> names = cacheNames();
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
        List<String> keys = cacheKey(cacheName);
        if (keys == null || keys.isEmpty()) {
            return Lists.newArrayList();
        }

        RedisConnection connection = redisConnectionFactory.getConnection();
        try {
            return keys.stream().map(key -> {
                byte[] redisKey = (KEY_PREFIX + cacheName + ":" + key).getBytes();
                String dataType = resolveDataType(connection, redisKey);
                Long ttl = connection.keyCommands().ttl(redisKey);
                String value = resolveValue(connection, redisKey, dataType);
                return new CacheKeyDetail(key, dataType, ttl, value);
            }).collect(Collectors.toList());
        } finally {
            connection.close();
        }
    }

    /**
     * 解析 key 类型
     */
    private String resolveDataType(RedisConnection connection, byte[] redisKey) {
        try {
            DataType type = connection.keyCommands().type(redisKey);
            return type == null ? "UNKNOWN" : type.code();
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }

    /**
     * 解析 key 值内容摘要
     */
    private String resolveValue(RedisConnection connection, byte[] redisKey, String dataType) {
        try {
            switch (dataType) {
                case "string":
                    byte[] value = connection.stringCommands().get(redisKey);
                    return truncate(value == null ? "" : StrUtil.str(value, "utf-8"));
                case "list":
                    List<byte[]> list = connection.listCommands().lRange(redisKey, 0, 9);
                    if (list == null || list.isEmpty()) {
                        return "[]";
                    }
                    return truncate(list.stream()
                        .map(item -> StrUtil.str(item, "utf-8"))
                        .collect(Collectors.joining(", ", "[", "]")));
                case "set":
                    Set<byte[]> set = connection.setCommands().sMembers(redisKey);
                    if (set == null || set.isEmpty()) {
                        return "[]";
                    }
                    return truncate(set.stream()
                        .limit(10)
                        .map(item -> StrUtil.str(item, "utf-8"))
                        .collect(Collectors.joining(", ", "[", "]")));
                case "hash":
                    long hashSize = connection.hashCommands().hLen(redisKey);
                    return "HASH(" + hashSize + ")";
                case "zset":
                    long zsetSize = connection.zSetCommands().zCard(redisKey);
                    return "ZSET(" + zsetSize + ")";
                default:
                    return "N/A";
            }
        } catch (Exception e) {
            return "N/A";
        }
    }

    /**
     * 移除某个缓存下的单个 key
     */
    @Override
    public void removeCacheKey(String cacheName, String key) {
        RedisConnection connection = redisConnectionFactory.getConnection();
        try {
            byte[] redisKey = (KEY_PREFIX + cacheName + ":" + key).getBytes();
            connection.keyCommands().del(redisKey);
        } finally {
            connection.close();
        }
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
