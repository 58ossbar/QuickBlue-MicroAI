package com.budaos.common.redis.config;

import com.budaos.common.redis.repeatsubmit.PreventRepeatRedisTicket;
import com.budaos.common.web.repeatsubmit.AbstractPreventRepeatTicket;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.function.Function;

/**
 * 防重复提交 Redis 配置
 *
 * @author budaos
 * @since 2026-02-09
 */
@Configuration
@Slf4j
@ConditionalOnProperty(name = "spring.redis.enabled", havingValue = "true", matchIfMissing = true)
public class PreventRepeatRedisConfig {

    /**
     * 生成防重复提交凭证的函数
     * 默认实现：基于请求路径、请求方法、参数、用户ID等生成唯一凭证
     */
    private static final Function<HttpServletRequest, String> DEFAULT_Ticket_FUNCTION = request -> {
        // 获取请求方法
        String method = request.getMethod();

        // 获取请求URI
        String uri = request.getRequestURI();

        // 获取请求参数
        String queryString = request.getQueryString();
        if (queryString == null) {
            queryString = "";
        }

        // 获取用户ID（从Header或Session中获取）
        String userId = request.getHeader("X-User-Id");
        if (userId == null || userId.isEmpty()) {
            userId = request.getHeader("userId");
        }
        if (userId == null || userId.isEmpty()) {
            userId = request.getSession().getId();
        }

        // 组合生成唯一凭证
        return String.format("repeat_submit:%s:%s:%s:%s", userId, method, uri, queryString);
    };

    /**
     * 防重复提交凭证（Redis实现）
     *
     * @param redisTemplate Redis模板
     * @return AbstractPreventRepeatTicket
     */
    @Bean(name = "repeatSubmitRedisTicket")
    @ConditionalOnMissingBean(name = "repeatSubmitRedisTicket")
    public AbstractPreventRepeatTicket repeatSubmitRedisTicket(RedisTemplate<String, Object> redisTemplate) {
        if (redisTemplate != null) {
            log.info("初始化防重复提交功能（Redis实现）");
            return new PreventRepeatRedisTicket(redisTemplate, DEFAULT_Ticket_FUNCTION);
        } else {
            log.warn("Redis未配置，无法初始化防重复提交Redis实现");
            return null;
        }
    }

}
