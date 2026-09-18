package com.budaos.common.web.repeatsubmit;

import jakarta.servlet.http.HttpServletRequest;

import java.util.function.Function;

/**
 * 凭证（用于校验重复提交的东西）
 *
 * @author budaos
 * @since 2026-02-09
 */
public abstract class AbstractPreventRepeatTicket {

    private final Function<HttpServletRequest, String> generateTicketFunction;


    public AbstractPreventRepeatTicket(Function<HttpServletRequest, String> generateTicketFunction) {
        this.generateTicketFunction = generateTicketFunction;
    }


    /**
     * 生成 加锁的 凭证
     */
    public String generateTicket(HttpServletRequest request) {
        return this.generateTicketFunction.apply(request);
    }

    /**
     * 加锁
     *
     * @param Ticket 凭证
     * @param currentTimestamp 当前时间戳
     * @param intervalMilliSecond 间隔时间（毫秒）
     * @return 是否加锁成功
     */
    public abstract boolean tryLock(String Ticket, Long currentTimestamp, Long intervalMilliSecond);

    /**
     * 移除锁
     *
     * @param Ticket 凭证
     * @param intervalMilliSecond 间隔时间（毫秒）
     */
    public abstract void unLock(String Ticket, Long intervalMilliSecond);

}
