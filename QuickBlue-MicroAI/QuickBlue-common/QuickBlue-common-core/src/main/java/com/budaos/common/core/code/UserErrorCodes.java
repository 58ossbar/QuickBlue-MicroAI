package com.budaos.common.core.code;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户级别的错误码（用户引起的错误返回码，可以不用关注）
 *
 */
@Getter
@AllArgsConstructor
public enum UserErrorCodes implements IErrorCode {

    // ============ 参数错误 30001 ============
    /**
     * 提交的参数不合法
     */
    PARAM_ERROR(30001, "参数有误，请检查后再试~"),

    // ============ 数据错误 30002-30004 ============
    /**
     * 数据不存在
     */
    DATA_NOT_EXIST(30002, "数据不存在~"),

    /**
     * 数据已被删除
     */
    DATA_DELETED(30003, "数据已被删除~"),

    /**
     * 数据已存在
     */
    ALREADY_EXIST(30004, "数据已存在~"),

    // ============ 重复提交 30005、30010 ============
    /**
     * 操作过快触发防重
     */
    REPEAT_SUBMIT(30005, "操作太快，请稍后再试~"),

    /**
     * 表单重复提交
     */
    FORM_REPEAT_SUBMIT(30010, "请勿重复提交~"),

    // ============ 权限错误 30006 ============
    /**
     * 无访问权限
     */
    NO_PERMISSION(30006, "您没有权限访问~"),

    // ============ 功能状态 30007 ============
    /**
     * 功能开发中
     */
    DEVELOPING(30007, "功能开发中，敬请期待~"),

    // ============ 登录与会话错误 30008-30013 ============
    /**
     * 登录状态失效
     */
    LOGIN_STATE_INVALID(30008, "登录已失效，请重新登录~"),

    /**
     * 用户状态异常
     */
    USER_STATUS_ERROR(30009, "账号状态异常，请联系管理员~"),

    /**
     * 登录失败已达上限被锁定
     */
    LOGIN_FAIL_LOCK(30011, "登录失败过多，账号已锁定~"),

    /**
     * 登录失败即将触发锁定
     */
    LOGIN_FAIL_WILL_LOCK(30012, "再失败将锁定账号~"),

    /**
     * 会话超时
     */
    LOGIN_ACTIVE_TIMEOUT(30013, "长时间未操作，请重新登录~");

    private final int code;

    private final String msg;

    private final String level;

    UserErrorCodes(int code, String msg) {
        this.code = code;
        this.msg = msg;
        this.level = LEVEL_USER;
    }
}
