package com.budaos.common.core.constant;

/**
 * 字符串常量
 */
public class StringConstants {

    /**
     * 逗号分隔符
     */
    public static final String SEPARATOR = ",";

    /**
     * 空字符串
     */
    public static final String EMPTY = "";

    /**
     * 冒号
     */
    public static final String COLON = ":";

    /**
     * Sa-Token loginId 分隔符，格式：userType + 分隔符 + userId
     *
     * <p>Sa-Token 1.46 起 checkLoginArgs 禁止 loginId 包含冒号（:），
     * 因此 loginId 的分隔符由冒号改为下划线。</p>
     */
    public static final String LOGIN_ID_SEPARATOR = "_";

}
