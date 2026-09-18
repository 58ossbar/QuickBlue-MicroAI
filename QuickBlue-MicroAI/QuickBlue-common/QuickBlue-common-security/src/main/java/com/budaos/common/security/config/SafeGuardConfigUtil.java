package com.budaos.common.security.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 三级等保配置工具类
 * <p>
 * 提供统一的三级等保配置访问入口
 *
 * @author budaos
 * @since 2026-02-19
 */
@Slf4j
@Component
public class SafeGuardConfigUtil {

    private static SafeGuardConfig config;

    public SafeGuardConfigUtil(SafeGuardConfig level3ProtectConfig) {
        SafeGuardConfigUtil.config = level3ProtectConfig;
        log.info("三级等保配置初始化完成");
    }

    /**
     * 获取三级等保配置
     *
     * @return 配置对象
     */
    public static SafeGuardConfig getConfig() {
        return config;
    }

    /**
     * 是否开启双因子登录
     *
     * @return true-开启, false-关闭
     */
    public static boolean isTwoFactorLoginEnabled() {
        return config.isTwoFactorLoginEnabled();
    }

    /**
     * 获取连续登录失败最大次数
     *
     * @return 登录失败次数，-1表示不限制
     */
    public static int getLoginFailMaxTimes() {
        return config.getLoginFailMaxTimes();
    }

    /**
     * 获取连续登录失败锁定时间（秒）
     *
     * @return 锁定时间（秒），-1表示不锁定
     */
    public static int getLoginFailLockSeconds() {
        return config.getLoginFailLockSeconds();
    }

    /**
     * 获取登录活跃超时时间（秒）
     *
     * @return 超时时间（秒），-1表示不限制
     */
    public static int getLoginActiveTimeoutSeconds() {
        return config.getLoginActiveTimeoutSeconds();
    }

    /**
     * 是否开启密码复杂度校验
     *
     * @return true-开启, false-关闭
     */
    public static boolean isPasswordComplexityEnabled() {
        return config.isPasswordComplexityEnabled();
    }

    /**
     * 获取定期修改密码天数
     *
     * @return 天数
     */
    public static int getRegularChangePasswordDays() {
        return config.getRegularChangePasswordDays();
    }

    /**
     * 获取定期修改密码不允许重复的次数
     *
     * @return 次数
     */
    public static int getRegularChangePasswordNotAllowRepeatTimes() {
        return config.getRegularChangePasswordNotAllowRepeatTimes();
    }

    /**
     * 获取文件大小限制（MB）
     *
     * @return 文件大小限制（MB）
     */
    public static long getMaxUploadFileSizeMb() {
        return config.getMaxUploadFileSizeMb();
    }

    /**
     * 获取文件大小限制（字节）
     *
     * @return 文件大小限制（字节）
     */
    public static long getMaxUploadFileSizeBytes() {
        return config.getMaxUploadFileSizeMb() * 1024L * 1024L;
    }

    /**
     * 是否开启文件检测
     *
     * @return true-开启, false-关闭
     */
    public static boolean isFileDetectFlag() {
        return config.isFileDetectFlag();
    }
}
