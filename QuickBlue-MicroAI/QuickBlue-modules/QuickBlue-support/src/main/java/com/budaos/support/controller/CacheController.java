package com.budaos.support.controller;

import com.budaos.common.core.domain.ApiResult;
import com.budaos.common.web.annotation.AuditLog;
import com.budaos.support.service.CacheStore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 缓存管理控制器
 *
 * @author budaos
 */
@Tag(name = "缓存管理", description = "缓存管理相关接口")
@RestController
@RequestMapping("/cache")
@AuditLog(module = "缓存管理", description = "缓存操作")
public class CacheController {

    @Resource
    private CacheStore cacheService;

    /**
     * 获取所有缓存名称
     */
    @Operation(summary = "获取所有缓存名称")
    @GetMapping("/names")
    public ApiResult<List<String>> cacheNames() {
        return ApiResult.ok(cacheService.cacheNames());
    }

    /**
     * 移除某个缓存
     */
    @Operation(summary = "移除某个缓存")
    @GetMapping("/remove/{cacheName}")
    public ApiResult<String> removeCache(@PathVariable String cacheName) {
        cacheService.removeCache(cacheName);
        return ApiResult.ok();
    }

    /**
     * 获取某个缓存的所有key
     */
    @Operation(summary = "获取某个缓存的所有key")
    @GetMapping("/keys/{cacheName}")
    public ApiResult<List<String>> cacheKeys(@PathVariable String cacheName) {
        return ApiResult.ok(cacheService.cacheKey(cacheName));
    }

    /**
     * 获取所有缓存信息（名称 + key数量）
     */
    @Operation(summary = "获取所有缓存信息（名称 + key数量）")
    @GetMapping("/list")
    public ApiResult<List<CacheStore.CacheInfo>> cacheList() {
        return ApiResult.ok(cacheService.cacheList());
    }

    /**
     * 获取某个缓存下所有 key 的详情
     */
    @Operation(summary = "获取某个缓存下所有key的详情")
    @GetMapping("/keys-detail/{cacheName}")
    public ApiResult<List<CacheStore.CacheKeyDetail>> cacheKeyDetails(@PathVariable String cacheName) {
        return ApiResult.ok(cacheService.cacheKeyDetails(cacheName));
    }

    /**
     * 移除某个缓存下的单个 key
     */
    @Operation(summary = "移除某个缓存下的单个key")
    @GetMapping("/remove-key/{cacheName}/{key}")
    public ApiResult<String> removeCacheKey(@PathVariable String cacheName, @PathVariable String key) {
        cacheService.removeCacheKey(cacheName, key);
        return ApiResult.ok();
    }
}
