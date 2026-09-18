package com.budaos.support.service;

import java.util.List;

/**
 * 缓存服务接口
 */
public interface CacheStore {

    /**
     * 获取所有缓存名称
     */
    List<String> cacheNames();

    /**
     * 某个缓存下的所有 key
     */
    List<String> cacheKey(String cacheName);

    /**
     * 移除某个缓存
     */
    void removeCache(String cacheName);

    /**
     * 获取所有缓存信息（名称 + key 数量）
     */
    List<CacheInfo> cacheList();

    /**
     * 获取某个缓存下所有 key 的详情
     */
    List<CacheKeyDetail> cacheKeyDetails(String cacheName);

    /**
     * 移除某个缓存下的单个 key
     */
    void removeCacheKey(String cacheName, String key);

    /**
     * 缓存信息
     */
    class CacheInfo {

        private String cacheName;

        private long keyCount;

        public CacheInfo() {
        }

        public CacheInfo(String cacheName, long keyCount) {
            this.cacheName = cacheName;
            this.keyCount = keyCount;
        }

        public String getCacheName() {
            return cacheName;
        }

        public void setCacheName(String cacheName) {
            this.cacheName = cacheName;
        }

        public long getKeyCount() {
            return keyCount;
        }

        public void setKeyCount(long keyCount) {
            this.keyCount = keyCount;
        }
    }

    /**
     * 缓存 key 详情
     */
    class CacheKeyDetail {

        private String key;

        /** 数据类型：STRING / HASH / LIST / SET / ZSET / OBJECT */
        private String dataType;

        /** 剩余过期时间（秒），-1 表示永不过期 */
        private Long ttl;

        /** 内容摘要 */
        private String value;

        public CacheKeyDetail() {
        }

        public CacheKeyDetail(String key, String dataType, Long ttl, String value) {
            this.key = key;
            this.dataType = dataType;
            this.ttl = ttl;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getDataType() {
            return dataType;
        }

        public void setDataType(String dataType) {
            this.dataType = dataType;
        }

        public Long getTtl() {
            return ttl;
        }

        public void setTtl(Long ttl) {
            this.ttl = ttl;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
