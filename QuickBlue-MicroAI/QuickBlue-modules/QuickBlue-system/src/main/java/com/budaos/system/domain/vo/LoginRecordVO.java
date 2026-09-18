package com.budaos.system.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录日志VO
 *
 * @author budaos
 */
@Data
@Schema(description = "登录日志VO")
public class LoginRecordVO {

    @Schema(description = "登录日志ID")
    private Long loginLogId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户类型")
    private Integer userType;

    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "登录IP")
    private String loginIp;

    @Schema(description = "登录IP地区")
    private String loginIpRegion;

    @Schema(description = "User-Agent")
    private String userAgent;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "登录设备")
    private String loginDevice;

    @Schema(description = "登录结果")
    private Integer loginResult;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
