package com.budaos.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 登录失败记录实体
 *
 * @author budaos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_login_fail")
public class LoginAttemptEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long loginFailId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户类型
     */
    private Integer userType;

    /**
     * 登录名
     */
    private String loginName;

    /**
     * 锁定状态
     */
    private Boolean lockFlag;

    /**
     * 登录失败次数
     */
    private Integer loginFailCount;

    /**
     * 连续登录失败锁定开始时间
     */
    private LocalDateTime loginLockBeginTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
