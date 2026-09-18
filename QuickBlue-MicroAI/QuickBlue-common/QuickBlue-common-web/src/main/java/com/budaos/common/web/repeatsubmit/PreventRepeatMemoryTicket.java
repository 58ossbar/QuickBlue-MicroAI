package com.budaos.common.web.repeatsubmit;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.google.common.collect.Maps;
import jakarta.servlet.http.HttpServletRequest;

import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * 凭证（内存实现）
 *
 * @author budaos
 * @since 2026-02-09
 */
public class PreventRepeatMemoryTicket extends AbstractPreventRepeatTicket {

    /**
     * 字符串常量池，用于实现细粒度的锁
     */
    private Interner<String> pool = Interners.newStrongInterner();

    /**
     * 凭证Map，记录每个凭证的最后执行时间
     */
    private ConcurrentMap<String, Long> TicketMap = Maps.newConcurrentMap();

    public PreventRepeatMemoryTicket(Function<HttpServletRequest, String> TicketFunction) {
        super(TicketFunction);
    }

    @Override
    public boolean tryLock(String Ticket, Long currentTimestamp, Long intervalMilliSecond) {
        // 使用字符串常量池实现细粒度锁
        synchronized (pool.intern(Ticket)) {
            // 尝试放入当前时间戳，如果不存在则返回null
            Long lastTime = TicketMap.putIfAbsent(Ticket, currentTimestamp);
            if (lastTime == null) {
                // 第一次执行，加锁成功
                return true;
            }

            // 如果间隔时间为0，表示只有上个请求执行完以后才可以执行
            if (intervalMilliSecond <= 0) {
                return false;
            }

            // 如果距离上次执行时间小于间隔时间，则不允许执行
            if (currentTimestamp - lastTime < intervalMilliSecond) {
                return false;
            }

            // 更新执行时间
            TicketMap.put(Ticket, currentTimestamp);
            return true;
        }
    }

    @Override
    public void unLock(String Ticket, Long intervalMilliSecond) {
        // 如果设置了间隔时间，则不需要手动删除（等待过期）
        if (intervalMilliSecond > 0) {
            return;
        }

        // 否则手动删除锁
        TicketMap.remove(Ticket);
    }


}
