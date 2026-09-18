package com.budaos.support.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.budaos.api.support.dto.SafeGuardConfigDTO;
import com.budaos.support.constant.SystemConfigKeyEnum;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.common.web.annotation.AuditLog;
import com.budaos.support.securityprotect.domain.SafeGuardConfigForm;
import com.budaos.support.service.SafeGuardConfigService;
import com.budaos.support.service.SystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 三级等保管理控制器
 *
 * @author budaos
 */
@Tag(name = "三级等保管理")
@RestController
@RequestMapping("/protect")
@AuditLog(module = "三级等保管理", description = "三级等保操作")
public class ProtectController {

    @Resource
    private SafeGuardConfigService level3ProtectConfigService;

    @Resource
    private SystemConfigService configService;

    @Operation(summary = "更新三级等保配置")
    @PostMapping("/level3protect/updateConfig")
    @SaCheckPermission("protect:level3:update")
    public ApiResult<String> updateConfig(@RequestBody @Valid SafeGuardConfigForm configForm) {
        return level3ProtectConfigService.updateLevel3Config(configForm);
    }

    @Operation(summary = "查询三级等保配置")
    @GetMapping("/level3protect/getConfig")
    public ApiResult<String> getConfig() {
        return ApiResult.ok(configService.getConfigValue(SystemConfigKeyEnum.LEVEL3_PROTECT_CONFIG));
    }

    /**
     * 获取三级等保配置详细对象（供 Feign 调用）
     */
    @Operation(summary = "获取三级等保配置对象")
    @GetMapping("/level3protect/getConfigObject")
    public ApiResult<SafeGuardConfigDTO> getConfigObject() {
        String configJson = configService.getConfigValue(SystemConfigKeyEnum.LEVEL3_PROTECT_CONFIG);

        SafeGuardConfigDTO config = new SafeGuardConfigDTO();
        config.setPasswordComplexityEnabled(level3ProtectConfigService.isPasswordComplexityEnabled());
        config.setRegularChangePasswordNotAllowRepeatTimes(level3ProtectConfigService.getRegularChangePasswordNotAllowRepeatTimes());
        config.setRegularChangePasswordMonths(level3ProtectConfigService.getRegularChangePasswordDays());
        config.setMaxUploadFileSizeMb(level3ProtectConfigService.getMaxUploadFileSizeMb());
        config.setFileDetectFlag(level3ProtectConfigService.isFileDetectFlag());
        config.setLoginFailMaxTimes(level3ProtectConfigService.getLoginFailMaxTimes());
        config.setLoginFailLockMinutes(level3ProtectConfigService.getLoginFailLockSeconds());

        return ApiResult.ok(config);
    }

    /**
     * 是否开启密码复杂度校验
     */
    @GetMapping("/level3protect/isPasswordComplexityEnabled")
    @Operation(summary = "是否开启密码复杂度校验")
    public ApiResult<Boolean> isPasswordComplexityEnabled() {
        return ApiResult.ok(level3ProtectConfigService.isPasswordComplexityEnabled());
    }

    /**
     * 获取定期修改密码不允许重复的次数
     */
    @GetMapping("/level3protect/getRegularChangePasswordNotAllowRepeatTimes")
    @Operation(summary = "获取定期修改密码不允许重复的次数")
    public ApiResult<Integer> getRegularChangePasswordNotAllowRepeatTimes() {
        return ApiResult.ok(level3ProtectConfigService.getRegularChangePasswordNotAllowRepeatTimes());
    }

    /**
     * 获取定期修改密码天数
     */
    @GetMapping("/level3protect/getRegularChangePasswordDays")
    @Operation(summary = "获取定期修改密码天数")
    public ApiResult<Integer> getRegularChangePasswordDays() {
        return ApiResult.ok(level3ProtectConfigService.getRegularChangePasswordDays());
    }

    /**
     * 获取连续登录失败最大次数
     */
    @GetMapping("/level3protect/getLoginFailMaxTimes")
    @Operation(summary = "获取连续登录失败最大次数")
    public ApiResult<Integer> getLoginFailMaxTimes() {
        return ApiResult.ok(level3ProtectConfigService.getLoginFailMaxTimes());
    }

    /**
     * 获取连续登录失败锁定时间（秒）
     */
    @GetMapping("/level3protect/getLoginFailLockSeconds")
    @Operation(summary = "获取连续登录失败锁定时间（秒）")
    public ApiResult<Integer> getLoginFailLockSeconds() {
        return ApiResult.ok(level3ProtectConfigService.getLoginFailLockSeconds());
    }
}
