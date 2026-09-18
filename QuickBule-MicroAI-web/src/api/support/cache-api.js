/*
 * 缓存
 *

 */
import { getRequest } from '/@/lib/axios';

export const cacheApi = {
    // 获取所有缓存（名称 + key数量）
    getAll: () => {
        return getRequest('/support/cache/list');
    },
    // 获取某个缓存的所有key
    getKeys: (cacheName) => {
        return getRequest(`/support/cache/keys/${encodeURIComponent(cacheName)}`);
    },
    // 获取某个缓存的所有key详情（含value摘要、ttl、类型）
    getKeyDetails: (cacheName) => {
        return getRequest(`/support/cache/keys-detail/${encodeURIComponent(cacheName)}`);
    },
    // 移除某个缓存
    remove: (cacheName) => {
        return getRequest(`/support/cache/remove/${encodeURIComponent(cacheName)}`);
    },
    // 移除某个缓存下的单个key
    removeKey: (cacheName, key) => {
        return getRequest(`/support/cache/remove-key/${encodeURIComponent(cacheName)}/${encodeURIComponent(key)}`);
    },
    // 获取所有缓存
    getAllCacheNames: () => {
        return getRequest('/support/cache/names');
    },
};
