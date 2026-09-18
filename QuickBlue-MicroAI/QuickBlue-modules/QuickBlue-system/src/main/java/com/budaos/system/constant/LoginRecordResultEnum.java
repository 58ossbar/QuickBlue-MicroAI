package com.budaos.system.constant;

/**
 * 登录类型枚举
 *
 * @author budaos
 * @since 2026-02-11
 */
public enum LoginRecordResultEnum {

    /**
     * 登录成功
     */
    LOGIN_SUCCESS(0, "登录成功"),

    /**
     * 登录失败
     */
    LOGIN_FAIL(1, "登录失败"),

    /**
     * 退出登录
     */
    LOGIN_OUT(2, "退出登录"),
    ;

    private final Integer value;

    private final String desc;

    LoginRecordResultEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public Integer getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }
}
