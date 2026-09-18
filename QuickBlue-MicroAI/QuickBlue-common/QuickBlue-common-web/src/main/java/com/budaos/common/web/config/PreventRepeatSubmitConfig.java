package com.budaos.common.web.config;

import com.budaos.common.web.aspect.PreventRepeatSubmitAspect;
import com.budaos.common.web.repeatsubmit.AbstractPreventRepeatTicket;
import com.budaos.common.web.repeatsubmit.PreventRepeatMemoryTicket;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Function;

/**
 * 防重复提交配置
 *
 * @author budaos
 * @since 2026-02-09
 */
@Configuration
@Slf4j
public class PreventRepeatSubmitConfig {

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
     * 防重复提交凭证（内存实现）
     * 默认实现，当没有 Redis 实现时会使用这个
     *
     * @return AbstractRepeatSubmitQuickBlue
     */
    @Bean(name = "repeatSubmitMemoryTicket")
    @ConditionalOnMissingBean(AbstractPreventRepeatTicket.class)
    public AbstractPreventRepeatTicket repeatSubmitMemoryTicket() {
        log.info("初始化防重复提交功能（内存实现）");
        return new PreventRepeatMemoryTicket(DEFAULT_Ticket_FUNCTION);
    }

    /**
     * 防重复提交切面
     *
     * @param repeatSubmitTicket 防重复提交凭证
     * @return PreventRepeatSubmitAspect
     */
    @Bean
    public PreventRepeatSubmitAspect repeatSubmitAspect(AbstractPreventRepeatTicket repeatSubmitTicket) {
        log.info("初始化防重复提交切面，实现类: {}", repeatSubmitTicket.getClass().getSimpleName());
        return new PreventRepeatSubmitAspect(repeatSubmitTicket);
    }

}
