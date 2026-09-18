package com.budaos.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 网关自定义配置属性
 *
 * @author budaos
 */
@Data
@Component
@ConfigurationProperties(prefix = "gateway.custom")
public class CustomGatewayProperties {

    /**
     * 白名单路径（不需要认证）
     */
    private List<String> whitelist = new ArrayList<>();

    /**
     * 认证服务配置
     */
    private AuthService authService = new AuthService();

    /**
     * 超时配置
     */
    private Timeout timeout = new Timeout();

    @Data
    public static class AuthService {
        /**
         * 认证服务地址
         */
        private String url;
    }

    @Data
    public static class Timeout {
        /**
         * 连接超时时间（毫秒）
         */
        private Long connect = 5000L;

        /**
         * 读取超时时间（毫秒）
         */
        private Long read = 30000L;
    }
}
