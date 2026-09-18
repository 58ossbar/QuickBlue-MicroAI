package com.budaos.system.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录失败VO
 *
 * @author budaos
 */
@Data
@Schema(description = "登录失败VO")
public class LoginAttemptVO {

    @Schema(description = "登录失败ID")
    private Long loginFailId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户类型")
    private Integer userType;

    @Schema(description = "登录名")
    private String loginName;

    @Schema(description = "连续登录失败次数")
    private Integer loginFailCount;

    @Schema(description = "锁定状态:1锁定，0未锁定")
    private Integer lockFlag;

    @Schema(description = "连续登录失败锁定开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime loginLockBeginTime;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
