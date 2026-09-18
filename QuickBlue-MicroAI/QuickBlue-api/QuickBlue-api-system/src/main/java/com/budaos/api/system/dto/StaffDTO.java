package com.budaos.api.system.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 员工DTO
 *
 * @author budaos
 */
@Data
public class StaffDTO implements Serializable {

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
     * 性别（1-男 2-女）
     */
    private Integer gender;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 部门ID
     */
    private Long departmentId;

    /**
     * 部门名称
     */
    private String departmentName;

    /**
     * 岗位ID
     */
    private Long positionId;

    /**
     * 岗位名称
     */
    private String positionName;

    /**
     * 是否超级管理员
     */
    private Boolean administratorFlag;

    /**
     * 是否禁用
     */
    private Boolean disabledFlag;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
