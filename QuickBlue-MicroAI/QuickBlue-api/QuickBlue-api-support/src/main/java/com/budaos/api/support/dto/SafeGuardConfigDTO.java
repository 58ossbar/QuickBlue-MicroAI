package com.budaos.api.support.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 三级等保相关配置 DTO
 */
@Data
public class SafeGuardConfigDTO {

    @Schema(description = "连续登录失败次数则锁定")
    private Integer loginFailMaxTimes;

    @Schema(description = "连续登录失败锁定时间(单位:分钟)")
    private Integer loginFailLockMinutes;

    @Schema(description = "最低活跃时间(单位:分钟)")
    private Integer loginActiveTimeoutMinutes;

    @Schema(description = "开启双因子登录")
    private Boolean twoFactorLoginEnabled;

    @Schema(description = "密码复杂度 是否开启,默认:开启")
    private Boolean passwordComplexityEnabled;

    @Schema(description = "定期修改密码时间间隔(默认:月)")
    private Integer regularChangePasswordMonths;

    @Schema(description = "定期修改密码不允许重复次数,默认:3次以内密码不能相同")
    private Integer regularChangePasswordNotAllowRepeatTimes;

    @Schema(description = "文件检测,默认:不开启")
    private Boolean fileDetectFlag;

    @Schema(description = "文件大小限制,单位 mb,默认:50 mb")
    private Long maxUploadFileSizeMb;
}
