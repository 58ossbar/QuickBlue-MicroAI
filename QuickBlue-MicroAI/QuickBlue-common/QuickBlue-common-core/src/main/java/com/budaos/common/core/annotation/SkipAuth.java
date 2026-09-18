package com.budaos.common.core.annotation;

import java.lang.annotation.*;

/**
 * 无需登录注解
 * 添加到 Controller 方法上，表示该方法无需登录即可访问
 *
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SkipAuth {
}
