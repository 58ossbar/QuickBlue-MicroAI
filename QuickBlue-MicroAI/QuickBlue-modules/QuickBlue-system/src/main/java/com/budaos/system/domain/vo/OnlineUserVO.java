package com.budaos.system.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 在线用户VO
 *
 * @author budaos
 */
@Data
@Schema(description = "在线用户信息")
public class OnlineUserVO {

    @Schema(description = "登录ID")
    private String loginId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户类型")
    private Integer userType;

    @Schema(description = "Token")
    private String token;

    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "登录名")
    private String loginName;

    @Schema(description = "部门ID")
    private Long departmentId;

    @Schema(description = "部门名称")
    private String departmentName;

    @Schema(description = "登录时间")
    private LocalDateTime loginTime;

    @Schema(description = "在线时长(秒)")
    private Long onlineDuration;

    @Schema(description = "在线时长描述")
    private String onlineDurationDesc;
}
