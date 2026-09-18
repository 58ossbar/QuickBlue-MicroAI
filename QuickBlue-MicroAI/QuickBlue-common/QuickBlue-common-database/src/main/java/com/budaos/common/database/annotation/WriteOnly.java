package com.budaos.common.database.annotation;

import java.lang.annotation.*;

/**
 * 只写注解（用于指定主库数据源）
 *
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WriteOnly {

    /**
     * 数据源名称，默认为 master
     */
    String value() default "master";
}
