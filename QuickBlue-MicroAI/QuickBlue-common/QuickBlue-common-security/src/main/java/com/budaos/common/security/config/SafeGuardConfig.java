package com.budaos.common.security.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 三级等保配置
 * <p>
 * 从Nacos配置中心读取三级等保相关配置
 *
 * @author budaos
 * @since 2026-02-19
 */
@Getter
@Component
@ConfigurationProperties(prefix = "level3-protect")
public class SafeGuardConfig {

    /**
     * 开启双因子登录，默认：关闭
     */
    private boolean twoFactorLoginEnabled = false;

    /**
     * 连续登录失败次数则锁定，-1表示不受限制，可以一直尝试登录，默认：5次
     */
    private int loginFailMaxTimes = 5;

    /**
     * 连续登录失败锁定时间（单位：秒），-1表示不锁定，建议锁定30分钟，默认：1800秒
     */
    private int loginFailLockSeconds = 1800;

    /**
     * 最低活跃时间（单位：秒），超过此时间没有操作系统就会被冻结，默认-1 代表不限制，永不冻结; 默认 30分钟
     */
    private int loginActiveTimeoutSeconds = -1;

    /**
     * 密码复杂度 是否开启，默认：开启
     */
    private boolean passwordComplexityEnabled = true;

    /**
     * 定期修改密码时间间隔（默认：天），默认：建议90天更换密码
     */
    private int regularChangePasswordDays = 90;

    /**
     * 定期修改密码不允许相同次数，默认：3次以内密码不能相同
     */
    private int regularChangePasswordNotAllowRepeatTimes = 3;

    /**
     * 文件大小限制，单位 MB，(默认：50 MB)
     */
    private long maxUploadFileSizeMb = 50;

    /**
     * 文件检测，默认：不开启
     */
    private boolean fileDetectFlag = false;

    /**
     * 获取最低活跃时间（单位：秒），超过此时间没有操作系统就会被冻结
     *
     * @return 活跃超时秒数，-1表示不限制
     */
    public int getLoginActiveTimeoutSeconds() {
        return loginActiveTimeoutSeconds > 0 ? loginActiveTimeoutSeconds : -1;
    }
}
