package com.budaos.common.core.util;

import org.springframework.beans.BeanUtils;

/**
 * Bean工具类
 *
 * @author budaos
 * @since 2026-02-11
 */
public class BeanUtil {

    /**
     * 复制属性
     *
     * @param source 源对象
     * @param target 目标对象
     */
    public static void copyProperties(Object source, Object target) {
        BeanUtils.copyProperties(source, target);
    }

    /**
     * 复制属性并返回目标对象
     *
     * @param source      源对象
     * @param targetClass 目标类
     * @param <T>        目标类型
     * @return 目标对象
     */
    public static <T> T copyProperties(Object source, Class<T> targetClass) {
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(source, target);
            return target;
        } catch (Exception e) {
            throw new RuntimeException("复制属性失败", e);
        }
    }
}
