package com.budaos.support.service;

import com.budaos.common.core.domain.CurrentUser;
import com.budaos.common.core.domain.ApiResult;

import java.util.List;

/**
 * 三级等保 密码 相关服务
 */
public interface SecurePasswordService {

    /**
     * 密码长度8-20位且包含大小写字母、数字、特殊符号三种及以上组合
     */
    String PASSWORD_PATTERN = "^(?![a-zA-Z]+$)(?![A-Z0-9]+$)(?![A-Z\\W_!@#$%^&*`~()-+=]+$)(?![a-z0-9]+$)(?![a-z\\W_!@#$%^&*`~()-+=]+$)(?![0-9\\W_!@#$%^&*`~()-+=]+$)[a-zA-Z0-9\\W_!@#$%^&*`~()-+=]*$";

    String PASSWORD_FORMAT_MSG = "密码必须为长度8-20位且必须包含大小写字母、数字、特殊符号（如：@#$%^&*()_+-=）等三种字符";

    /**
     * 校验密码复杂度
     */
    ApiResult<String> validatePasswordComplexity(String password);

    /**
     * 校验密码重复次数
     */
    ApiResult<String> validatePasswordRepeatTimes(CurrentUser requestUser, String newPassword);

    /**
     * 随机生成密码
     */
    String randomPassword();

    /**
     * 保存修改密码
     */
    void saveUserChangePasswordLog(CurrentUser requestUser, String newPassword, String oldPassword);

    /**
     * 检查是否需要修改密码
     */
    boolean checkNeedChangePassword(Integer userType, Long userId);

    /**
     * 获取加密后的密码
     */
    static String getEncryptPwd(String password) {
        return Argon2PasswordEncoderHolder.ARGON2_PASSWORD_ENCODER.encode(password);
    }

    /**
     * 校验密码是否匹配
     */
    static Boolean matchesPwd(String password, String encodedPassword) {
        return Argon2PasswordEncoderHolder.ARGON2_PASSWORD_ENCODER.matches(password, encodedPassword);
    }

    /**
     * Argon2PasswordEncoder 持有者
     */
    class Argon2PasswordEncoderHolder {
        static final org.springframework.security.crypto.argon2.Argon2PasswordEncoder ARGON2_PASSWORD_ENCODER =
                org.springframework.security.crypto.argon2.Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }
}
