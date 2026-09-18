package com.budaos.common.database.annotation;

import java.lang.annotation.*;

/**
 * 只读注解（用于指定从库数据源）
 *
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ReadOnly {

    /**
     * 数据源名称，为空时使用随机从库
     */
    String value() default "";
}
