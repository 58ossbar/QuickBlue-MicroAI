package com.budaos.support.securityprotect.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.budaos.support.constant.SystemConfigKeyEnum;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.support.securityprotect.domain.SafeGuardConfigForm;
import com.budaos.support.service.SystemConfigService;
import com.budaos.support.service.SafeGuardConfigService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 三级等保配置实现
 */
@Service
@Slf4j
public class SafeGuardConfigServiceImpl implements com.budaos.support.service.SafeGuardConfigService {

    /**
     * 开启双因子登录，默认：开启
     */
    @Getter
    private boolean twoFactorLoginEnabled = false;

    /**
     * 连续登录失败次数则锁定，-1表示不受限制，可以一直尝试登录
     */
    @Getter
    private int loginFailMaxTimes = -1;

    /**
     * 连续登录失败锁定时间（单位：秒），-1表示不锁定，建议锁定30分钟
     */
    @Getter
    private int loginFailLockSeconds = 1800;

    /**
     * 最低活跃时间（单位：秒），超过此时间没有操作系统就会被冻结，默认-1 代表不限制，永不冻结; 默认 30分钟
     */
    private int loginActiveTimeoutSeconds = -1;

    /**
     * 密码复杂度 是否开启，默认：开启
     */
    @Getter
    private boolean passwordComplexityEnabled = true;

    /**
     * 定期修改密码时间间隔（默认：天），默认：建议90天更换密码
     */
    @Getter
    private int regularChangePasswordDays = 90;

    /**
     * 定期修改密码不允许相同次数，默认：3次以内密码不能相同
     */
    @Getter
    private int regularChangePasswordNotAllowRepeatTimes = 3;

    /**
     * 文件大小限制，单位 mb ，(默认：50 mb)
     */
    @Getter
    private long maxUploadFileSizeMb = 50;

    /**
     * 文件检测，默认：不开启
     */
    @Getter
    private boolean fileDetectFlag = false;

    @Resource
    private SystemConfigService configService;

    /**
     * 最低活跃时间（单位：秒），超过此时间没有操作系统就会被冻结，默认-1 代表不限制，永不冻结; 默认 30分钟
     */
    public int getLoginActiveTimeoutSeconds() {
        return loginActiveTimeoutSeconds > 0 ? loginActiveTimeoutSeconds : -1;
    }

    @PostConstruct
    void init() {
        try {
            String configValue = configService.getConfigValue(SystemConfigKeyEnum.LEVEL3_PROTECT_CONFIG);
            if (StrUtil.isEmpty(configValue)) {
                log.warn("t_config 表 三级等保配置为空，使用默认配置！");
                // 使用默认值，不抛出异常
                return;
            }
            try {
                SafeGuardConfigForm level3ProtectConfigForm = JSON.parseObject(configValue, SafeGuardConfigForm.class);
                setProp(level3ProtectConfigForm);
            } catch (Exception e) {
                log.error("解析三级等保配置失败，使用默认配置", e);
            }
        } catch (Exception e) {
            log.error("初始化三级等保配置失败，使用默认配置", e);
            // 不抛出异常，使用默认值继续启动
        }
    }

    /**
     * 设置属性
     */
    private void setProp(SafeGuardConfigForm configForm) {

        if (configForm.getFileDetectFlag() != null) {
            this.fileDetectFlag = configForm.getFileDetectFlag();
        }

        if (configForm.getMaxUploadFileSizeMb() != null) {
            this.maxUploadFileSizeMb = configForm.getMaxUploadFileSizeMb();
        }

        if (configForm.getLoginFailMaxTimes() != null) {
            this.loginFailMaxTimes = configForm.getLoginFailMaxTimes();
        }

        if (configForm.getLoginFailLockMinutes() != null) {
            this.loginFailLockSeconds = configForm.getLoginFailLockMinutes() * 60;
        }

        if (configForm.getLoginActiveTimeoutMinutes() != null) {
            this.loginActiveTimeoutSeconds = configForm.getLoginActiveTimeoutMinutes() * 60;
            this.loginActiveTimeoutSeconds = loginActiveTimeoutSeconds > 0 ? loginActiveTimeoutSeconds : -1;
        }

        if (configForm.getPasswordComplexityEnabled() != null) {
            this.passwordComplexityEnabled = configForm.getPasswordComplexityEnabled();
        }

        if (configForm.getRegularChangePasswordMonths() != null) {
            this.regularChangePasswordDays = configForm.getRegularChangePasswordMonths() * 30;
        }

        if (configForm.getTwoFactorLoginEnabled() != null) {
            this.twoFactorLoginEnabled = configForm.getTwoFactorLoginEnabled();
        }

        if (configForm.getRegularChangePasswordNotAllowRepeatTimes() != null) {
            this.regularChangePasswordNotAllowRepeatTimes = configForm.getRegularChangePasswordNotAllowRepeatTimes();
        }

        // 设置 最低活跃时间（单位：秒）
        try {
            if (this.loginActiveTimeoutSeconds > 0) {
                StpUtil.getStpLogic().getConfigOrGlobal().setActiveTimeout(getLoginActiveTimeoutSeconds());
            } else {
                StpUtil.getStpLogic().getConfigOrGlobal().setActiveTimeout(-1);
            }
        } catch (Exception e) {
            log.warn("设置Sa-Token活跃超时配置失败: {}", e.getMessage());
        }
    }

    /**
     * 更新三级等保配置
     */
    public ApiResult<String> updateLevel3Config(SafeGuardConfigForm configForm) {
        // 设置属性
        setProp(configForm);
        // 保存数据库
        String configFormJsonString = JSON.toJSONString(configForm, true);
        return configService.updateValueByKey(SystemConfigKeyEnum.LEVEL3_PROTECT_CONFIG, configFormJsonString);
    }
}
