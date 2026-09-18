package com.budaos.api.support.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志DTO
 *
 * @author budaos
 */
@Data
public class AuditLogDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    private Long logId;

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 员工姓名
     */
    private String employeeName;

    /**
     * 操作模块
     */
    private String module;

    /**
     * 操作类型
     */
    private String operateType;

    /**
     * 操作说明
     */
    private String description;

    /**
     * 请求路径
     */
    private String requestPath;

    /**
     * 请求方法
     */
    private String requestMethod;

    /**
     * 请求参数
     */
    private String requestParams;

    /**
     * 响应结果
     */
    private String responseResult;

    /**
     * 执行时长（毫秒）
     */
    private Long executeTime;

    /**
     * IP地址
     */
    private String ip;

    /**
     * User-Agent
     */
    private String userAgent;

    /**
     * 操作时间
     */
    private LocalDateTime createTime;
}
