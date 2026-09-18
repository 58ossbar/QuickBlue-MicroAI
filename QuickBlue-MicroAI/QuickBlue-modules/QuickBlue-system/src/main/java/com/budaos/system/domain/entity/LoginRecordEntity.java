package com.budaos.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录日志实体
 *
 * @author budaos
 */
@Data
@TableName("t_login_log")
public class LoginRecordEntity {

    /**
     * 日志ID
     */
    @TableId(type = IdType.AUTO)
    private Long loginLogId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 用户类型
     */
    private Integer userType;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 登录ip
     */
    private String loginIp;

    /**
     * 登录ip地区
     */
    private String loginIpRegion;

    /**
     * user-agent
     */
    private String userAgent;

    /**
     * 备注
     */
    private String remark;

    /**
     * 登录设备
     */
    private String loginDevice;

    /**
     * 登录结果 0-失败 1-成功 2-退出
     */
    private Integer loginResult;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
