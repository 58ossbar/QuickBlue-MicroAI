package com.budaos.common.redis.repeatsubmit;

import com.budaos.common.web.repeatsubmit.AbstractPreventRepeatTicket;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 防重复提交凭证（Redis实现）
 *
 * @author budaos
 * @since 2026-02-09
 */
public class PreventRepeatRedisTicket extends AbstractPreventRepeatTicket {

    private final RedisTemplate<String, Object> redisTemplate;

    public PreventRepeatRedisTicket(RedisTemplate<String, Object> redisTemplate,
                                   Function<HttpServletRequest, String> TicketFunction) {
        super(TicketFunction);
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean tryLock(String ticket, Long currentTimestamp, Long intervalMilliSecond) {
        if (intervalMilliSecond > 0) {
            // 指定时间内只能执行一次，使用 Redis 的 setIfAbsent 加上过期时间
            return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(
                    ticket, String.valueOf(currentTimestamp), intervalMilliSecond, TimeUnit.MILLISECONDS));
        } else {
            // 只有上个请求执行完以后才可以执行，不设置过期时间，执行完成后手动删除
            return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(
                    ticket, String.valueOf(currentTimestamp)));
        }
    }

    @Override
    public void unLock(String ticket, Long intervalMilliSecond) {
        // 如果设置了间隔时间，则不需要手动删除（Redis 会自动过期）
        if (intervalMilliSecond > 0) {
            return;
        }

        // 否则手动删除锁
        redisTemplate.delete(ticket);
    }

}
