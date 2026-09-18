package com.budaos.api.system.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 登录结果DTO
 *
 * @author budaos
 */
@Data
public class AuthLoginDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 员工UID
     */
    private String employeeUid;

    /**
     * 登录账号
     */
    private String loginName;

    /**
     * 员工姓名
     */
    private String actualName;

    /**
     * 头像
     */
    private String avatar;

    /**
     * Token
     */
    private String token;

    /**
     * Token值
     */
    private String tokenValue;

    /**
     * 登录时间
     */
    private Long loginTime;
}
