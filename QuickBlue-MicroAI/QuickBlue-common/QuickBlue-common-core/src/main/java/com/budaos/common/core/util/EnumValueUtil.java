package com.budaos.common.core.util;

import com.budaos.common.core.domain.BaseEnumeration;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * 枚举工具类
 *
 * @author budaos
 */
public class EnumValueUtil {

    /**
     * 根据值获取枚举
     *
     * @param value 值
     * @param clazz 枚举类
     * @param <T>   枚举类型
     * @return 枚举对象
     */
    public static <T extends BaseEnumeration> T getEnumByValue(Object value, Class<T> clazz) {
        if (value == null || clazz == null) {
            return null;
        }
        return Arrays.stream(clazz.getEnumConstants())
                .filter(t -> t.equalsValue(value))
                .findFirst()
                .orElse(null);
    }

    /**
     * 根据描述获取枚举
     *
     * @param desc  描述
     * @param clazz 枚举类
     * @param <T>   枚举类型
     * @return 枚举对象
     */
    public static <T extends BaseEnumeration> T getEnumByDesc(String desc, Class<T> clazz) {
        if (StringUtils.isBlank(desc) || clazz == null) {
            return null;
        }
        return Arrays.stream(clazz.getEnumConstants())
                .filter(t -> StringUtils.equals(t.getDesc(), desc))
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取枚举的所有描述
     *
     * @param clazz 枚举类
     * @param <T>   枚举类型
     * @return 描述数组
     */
    public static <T extends BaseEnumeration> String[] getDescArray(Class<T> clazz) {
        if (clazz == null) {
            return new String[0];
        }
        return Arrays.stream(clazz.getEnumConstants())
                .map(T::getDesc)
                .toArray(String[]::new);
    }

    /**
     * 获取枚举的所有值
     *
     * @param clazz 枚举类
     * @param <T>   枚举类型
     * @return 值数组
     */
    public static <T extends BaseEnumeration> Object[] getValueArray(Class<T> clazz) {
        if (clazz == null) {
            return new Object[0];
        }
        return Arrays.stream(clazz.getEnumConstants())
                .map(T::getValue)
                .toArray();
    }
}
