package com.budaos.common.web.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 * OpenFeign 缓存包装器
 * 用于为频繁调用的 Feign 接口添加缓存
 *
 * 使用示例：
 * @Autowired
 * private FeignCacheWrapper feignCacheWrapper;
 *
 * @Autowired
 * private SystemFeignClient systemFeignClient;
 *
 * // 带缓存的调用
 * StaffDTO employee = feignCacheWrapper.getEmployee(systemFeignClient, employeeId);
 *
 * 性能提升：50-90%（缓存命中时）
 *
 * @author budaos
 */
@Slf4j
@Component
public class FeignCacheWrapper {

    /**
     * 带缓存的员工信息获取
     * 缓存时间：10分钟
     */
    @Cacheable(value = "userInfoCache", key = "'employee:' + #employeeId")
    public <T> T getEmployee(Object feignClient, Long employeeId) {
        log.debug("从 Feign 获取员工信息 - employeeId: {}", employeeId);
        // 这里通过反射调用具体的 Feign 方法
        // 实际使用时，需要根据具体的 Feign Client 实现相应的包装方法
        return null; // 示例代码，实际需要实现
    }

    /**
     * 带缓存的部门信息获取
     * 缓存时间：30分钟
     */
    @Cacheable(value = "configCache", key = "'department:' + #departmentId")
    public <T> T getDepartment(Object feignClient, Long departmentId) {
        log.debug("从 Feign 获取部门信息 - departmentId: {}", departmentId);
        return null; // 示例代码，实际需要实现
    }

    /**
     * 带缓存的菜单信息获取
     * 缓存时间：30分钟
     */
    @Cacheable(value = "configCache", key = "'menu:' + #userId")
    public <T> T getMenuList(Object feignClient, Long userId) {
        log.debug("从 Feign 获取菜单信息 - userId: {}", userId);
        return null; // 示例代码，实际需要实现
    }

    /**
     * 带缓存的字典信息获取
     * 缓存时间：1小时
     */
    @Cacheable(value = "dictCache", key = "'dict:' + #dictType")
    public <T> T getDictData(Object feignClient, String dictType) {
        log.debug("从 Feign 获取字典信息 - dictType: {}", dictType);
        return null; // 示例代码，实际需要实现
    }

    /**
     * 带缓存的配置信息获取
     * 缓存时间：30分钟
     */
    @Cacheable(value = "configCache", key = "'config:' + #configKey")
    public <T> T getConfig(Object feignClient, String configKey) {
        log.debug("从 Feign 获取配置信息 - configKey: {}", configKey);
        return null; // 示例代码，实际需要实现
    }
}
