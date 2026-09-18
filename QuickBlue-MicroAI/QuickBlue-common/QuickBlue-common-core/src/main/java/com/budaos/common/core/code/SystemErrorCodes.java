package com.budaos.common.core.code;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 系统级别的错误码（系统引起的错误返回码，需要关注）
 *
 */
@Getter
@AllArgsConstructor
public enum SystemErrorCodes implements IErrorCode {

    // ============ 基础系统错误 50001-50099 ============
    SYSTEM_ERROR(50001, "系统错误，请联系管理员"),
    
    DB_ERROR(50002, "数据库错误"),

    FILE_ERROR(50004, "文件操作失败"),

    CONFIG_ERROR(50005, "配置错误"),

    // ============ 网络相关错误 50100-50199 ============
    /**
     * 服务不可用（服务未启动或已停止）
     */
    SERVICE_UNAVAILABLE(50100, "服务暂时不可用，请稍后重试"),
    
    /**
     * 服务超时
     */
    SERVICE_TIMEOUT(50101, "服务响应超时，请稍后重试"),
    
    /**
     * 网络连接失败
     */
    NETWORK_CONNECT_ERROR(50102, "网络连接失败，请检查网络"),
    
    /**
     * 网关错误
     */
    GATEWAY_ERROR(50103, "网关服务异常"),
    
    /**
     * 服务降级
     */
    SERVICE_DEGRADATION(50104, "服务繁忙，已触发降级保护"),

    // ============ 认证授权相关错误 50200-50299 ============
    /**
     * Token无效
     */
    TOKEN_INVALID(50200, "登录凭证无效，请重新登录"),
    
    /**
     * Token过期
     */
    TOKEN_EXPIRED(50201, "登录已过期，请重新登录"),
    
    /**
     * 权限不足
     */
    PERMISSION_DENIED(50202, "权限不足，无法访问"),

    // ============ 业务处理错误 50300-50399 ============
    /**
     * 业务处理异常
     */
    BUSINESS_ERROR(50300, "业务处理异常"),
    
    /**
     * 数据校验失败
     */
    DATA_VALIDATE_ERROR(50301, "数据校验失败"),
    
    /**
     * 并发操作冲突
     */
    CONCURRENT_ERROR(50302, "数据已被其他用户修改，请刷新后重试");

    private final int code;

    private final String msg;

    private final String level;

    SystemErrorCodes(int code, String msg) {
        this.code = code;
        this.msg = msg;
        this.level = LEVEL_SYSTEM;
    }
}
