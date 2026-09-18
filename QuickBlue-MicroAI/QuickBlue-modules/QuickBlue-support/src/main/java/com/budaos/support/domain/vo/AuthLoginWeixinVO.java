package com.budaos.support.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 微信端登录结果VO
 *
 * @author budaos
 */
@Data
@Schema(description = "微信端登录结果")
public class AuthLoginWeixinVO {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "登录名")
    private String loginName;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "部门名称")
    private String departmentName;

    @Schema(description = "上次登录IP")
    private String lastLoginIp;

    @Schema(description = "上次登录IP地区")
    private String lastLoginIpRegion;

    @Schema(description = "上次登录时间")
    private String lastLoginTime;

    @Schema(description = "上次登录User-Agent")
    private String lastLoginUserAgent;

    @Schema(description = "是否需要修改密码")
    private Boolean needUpdatePwdFlag;

    @Schema(description = "Token")
    private String token;

    @Schema(description = "所属影院ID")
    private String cinemaId;

    @Schema(description = "所属影院名称")
    private String cinemaName;
}
