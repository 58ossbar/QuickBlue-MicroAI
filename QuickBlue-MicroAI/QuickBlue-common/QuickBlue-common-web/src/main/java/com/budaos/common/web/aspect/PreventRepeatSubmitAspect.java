package com.budaos.common.web.aspect;

import com.budaos.common.core.domain.ApiResult;
import com.budaos.common.core.code.UserErrorCodes;
import com.budaos.common.web.annotation.PreventRepeatSubmit;
import com.budaos.common.web.repeatsubmit.AbstractPreventRepeatTicket;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * 防重复提交切面
 *
 * <p>拦截带有 {@link PreventRepeatSubmit} 注解的方法：基于请求生成唯一凭证，
 * 凭证在间隔时间内加锁成功则放行执行，否则判定为重复提交直接返回。</p>
 *
 * <p>间隔规则说明详见 {@link PreventRepeatSubmit#intervalMilliSecond()}。</p>
 *
 * @author budaos
 * @since 2026-02-09
 */
@Aspect
@Slf4j
public class PreventRepeatSubmitAspect {

    private final AbstractPreventRepeatTicket repeatSubmitTicket;

    /**
     * 构造函数
     *
     * @param repeatSubmitTicket 防重复提交凭证
     */
    public PreventRepeatSubmitAspect(AbstractPreventRepeatTicket repeatSubmitTicket) {
        this.repeatSubmitTicket = repeatSubmitTicket;
    }

    /**
     * 拦截所有带有 @PreventRepeatSubmit 注解的方法
     */
    @Around("@annotation(com.budaos.common.web.annotation.PreventRepeatSubmit)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            // 无请求上下文，直接放行
            return point.proceed();
        }

        // 第一步：基于请求生成防重复提交凭证
        HttpServletRequest request = attributes.getRequest();
        String ticket = this.repeatSubmitTicket.generateTicket(request);
        if (StringUtils.isEmpty(ticket)) {
            // 凭证为空，直接放行
            return point.proceed();
        }

        // 第二步：根据凭证加锁，加锁成功才允许执行目标方法
        Method method = ((MethodSignature) point.getSignature()).getMethod();
        PreventRepeatSubmit annotation = method.getAnnotation(PreventRepeatSubmit.class);
        long intervalMilliSecond = annotation.intervalMilliSecond();

        boolean lockSuccess = this.repeatSubmitTicket.tryLock(ticket, System.currentTimeMillis(), intervalMilliSecond);
        if (!lockSuccess) {
            // 加锁失败，说明仍处于间隔时间内，判定为重复提交
            log.warn("检测到重复提交: ticket={}", ticket);
            return ApiResult.error(UserErrorCodes.REPEAT_SUBMIT);
        }

        try {
            // 加锁成功，执行目标方法
            return point.proceed();
        } catch (Throwable throwable) {
            log.error("方法执行异常: {}", throwable.getMessage(), throwable);
            throw throwable;
        } finally {
            // 释放锁
            this.repeatSubmitTicket.unLock(ticket, intervalMilliSecond);
        }
    }
}
