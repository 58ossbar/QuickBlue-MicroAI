package com.budaos.api.support.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登录日志DTO
 *
 * @author budaos
 */
@Data
public class LoginRecordDTO implements Serializable {

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
     * 登录账号
     */
    private String loginName;

    /**
     * 登录结果（1-成功 2-失败）
     */
    private Integer result;

    /**
     * 失败原因
     */
    private String failReason;

    /**
     * IP地址
     */
    private String ip;

    /**
     * IP地区
     */
    private String ipRegion;

    /**
     * User-Agent
     */
    private String userAgent;

    /**
     * 登录时间
     */
    private LocalDateTime createTime;
}
