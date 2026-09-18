package com.budaos.common.security.config;

import org.springframework.context.annotation.Configuration;

/**
 * Sa-Token 自动配置类
 * <p>
 * 禁用Sa-Token的自动配置拦截器，使用各模块自定义的SaTokenConfig
 * </p>
 *
 * @author QuickBlue
 */
@Configuration
public class SaTokenAutoConfig {
    // 此配置类用于标记禁用Sa-Token自动配置的拦截器
    // 实际的拦截器配置在各模块的SaTokenConfig中
}
