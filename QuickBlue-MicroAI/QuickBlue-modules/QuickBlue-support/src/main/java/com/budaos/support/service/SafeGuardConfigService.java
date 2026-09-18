package com.budaos.support.service;

import com.budaos.common.core.domain.ApiResult;
import com.budaos.support.securityprotect.domain.SafeGuardConfigForm;

/**
 * 三级等保配置服务
 */
public interface SafeGuardConfigService {

    /**
     * 是否开启密码复杂度校验
     */
    boolean isPasswordComplexityEnabled();

    /**
     * 获取定期修改密码不允许重复的次数
     */
    int getRegularChangePasswordNotAllowRepeatTimes();

    /**
     * 获取定期修改密码天数
     */
    int getRegularChangePasswordDays();

    /**
     * 获取文件大小限制（单位 MB）
     */
    long getMaxUploadFileSizeMb();

    /**
     * 是否开启文件检测
     */
    boolean isFileDetectFlag();

    /**
     * 获取连续登录失败最大次数
     */
    int getLoginFailMaxTimes();

    /**
     * 获取连续登录失败锁定时间（秒）
     */
    int getLoginFailLockSeconds();

    /**
     * 更新三级等保配置
     */
    ApiResult<String> updateLevel3Config(SafeGuardConfigForm configForm);
}
