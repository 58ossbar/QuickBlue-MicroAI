package com.budaos.system.annotation;

import java.lang.annotation.*;

/**
 * 数据权限注解
 *
 * @author QuickBlue
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PermissionData {

    /**
     * 页面组件（用于前端权限控制）
     * 示例: "system/employee", "business/notice"
     */
    String pageComponent() default "";
}
