package com.budaos.common.core.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义的属性校验注解，为了方便与校验属性的值是否为合法的枚举值
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EnumCheckValidator.class)
public @interface EnumCheck {

    /**
     * 默认的错误提示信息
     */
    String message();

    /**
     * 枚举类对象 必须实现BaseEnum接口
     */
    Class<?> value();

    /**
     * 是否必须
     */
    boolean required() default false;

    //下面这两个属性必须添加 :不然会报错
    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
