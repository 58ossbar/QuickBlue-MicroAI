package com.budaos.common.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记 需要防止重复提交 的注解<br>
 * 单位：毫秒<br>
 * -------------------------<br>
 * 着重说明：<br>
 * 注解属性 intervalMilliSecond 是指 一段时间内只允许有一次请求；<br>
 * intervalMilliSecond = 0: 表示只有上个请求执行完以后才可以执行<br>
 * intervalMilliSecond > 0: 表示指定时间内只才能执行，特别提醒<br>
 * ------------------------<br>
 * 特殊说明 intervalMilliSecond > 0 <br>
 * 若假设 方法执行时间为 100ms，若 intervalMilliSecond = 50，则 同一时间内可能会有2个请求同时在执行！<br>
 * 若假设 方法执行时间为 100ms，若 intervalMilliSecond = 200，则 同一时间内只能有1请求执行，且执行完后100ms，才会执行下一次请求<br>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PreventRepeatSubmit {

    /**
     * 间隔时间/毫秒
     */
    int intervalMilliSecond() default 0;

}
