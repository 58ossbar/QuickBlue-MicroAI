package com.budaos.gateway.config;

import org.springframework.context.annotation.Configuration;

/**
 * 网关跨域配置
 *
 * 注意：CORS 配置已在 application.yaml 中通过 spring.cloud.gateway.globalcors 配置
 * 不要在这里注册 CorsWebFilter，否则会导致响应头重复设置
 *
 * @author budaos
 */
@Configuration
public class GatewayCorsConfig {
    // CORS 配置已移至 application.yaml
}
