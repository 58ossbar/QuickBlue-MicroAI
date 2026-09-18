package com.budaos.common.core.enums;

import lombok.Getter;

/**
 * 响应码枚举
 *
 * @author budaos
 */
@Getter
public enum ResponseCodeEnum {

    /**
     * 成功
     */
    SUCCESS("0", "操作成功"),

    /**
     * 用户错误
     */
    USER_ERROR("1", "用户错误"),

    /**
     * 系统错误
     */
    SYSTEM_ERROR("2", "系统错误"),

    /**
     * 参数校验失败
     */
    PARAM_ERROR("3", "参数校验失败"),

    /**
     * 未登录
     */
    NOT_LOGIN("10001", "请先登录"),

    /**
     * 没有权限
     */
    NO_PERMISSION("10002", "没有操作权限"),

    /**
     * 账号或密码错误
     */
    LOGIN_ERROR("10003", "账号或密码错误"),

    /**
     * 账号被禁用
     */
    ACCOUNT_DISABLED("10004", "账号已被禁用"),

    /**
     * Token过期
     */
    TOKEN_EXPIRED("10005", "登录已过期，请重新登录"),

    /**
     * Token无效
     */
    TOKEN_INVALID("10006", "登录已失效，请重新登录"),

    /**
     * 验证码错误
     */
    CAPTCHA_ERROR("10007", "验证码错误"),

    /**
     * 验证码过期
     */
    CAPTCHA_EXPIRED("10008", "验证码已过期"),

    /**
     * 数据不存在
     */
    DATA_NOT_EXIST("20001", "数据不存在"),

    /**
     * 数据已存在
     */
    DATA_ALREADY_EXIST("20002", "数据已存在"),

    /**
     * 数据已删除
     */
    DATA_ALREADY_DELETED("20003", "数据已删除");

    /**
     * 响应码
     */
    private final String code;

    /**
     * 响应消息
     */
    private final String message;

    ResponseCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
