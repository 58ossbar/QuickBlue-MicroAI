package com.budaos.common.core.util;

/**
 * 字符串工具类
 *
 * @author budaos
 * @since 2026-02-11
 */
public class StringUtil {

    /**
     * 检查字符串是否为空
     *
     * @param str 字符串
     * @return true-空 false-非空
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 检查字符串是否为非空
     *
     * @param str 字符串
     * @return true-非空 false-空
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 检查字符串是否为空白
     *
     * @param str 字符串
     * @return true-空白 false-非空白
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 检查字符串是否为非空白
     *
     * @param str 字符串
     * @return true-非空白 false-空白
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }
}
