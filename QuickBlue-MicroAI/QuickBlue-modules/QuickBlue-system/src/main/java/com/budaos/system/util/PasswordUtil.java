package com.budaos.system.util;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

/**
 * 密码工具类
 * <p>
 * 密码加密机制（与 QuickBlue-admin 保持一致）：
 * 1. 第一层：加盐处理，格式为：password_UID大写_UID小写
 * 2. 第二层：使用 Argon2 加密
 *
 * @author budaos
 */
public class PasswordUtil {

    /**
     * Argon2PasswordEncoder 持有者
     */
    static final Argon2PasswordEncoder ARGON2_PASSWORD_ENCODER =
            Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();

    /**
     * 生成加盐密码
     * <p>
     * 格式为：[password]_[uid大写]_[uid小写]
     *
     * @param password     明文密码
     * @param employeeUid 员工UID
     * @return 加盐后的密码（注意：此方法只进行加盐，不进行加密）
     */
    public static String generateSaltPassword(String password, String employeeUid) {
        return password + "_" + employeeUid.toUpperCase() + "_" + employeeUid.toLowerCase();
    }

    /**
     * 获取加密后的密码
     * <p>
     * 使用 Argon2 算法加密密码
     *
     * @param saltPassword 加盐后的密码
     * @return Argon2 加密后的密码
     */
    public static String getEncryptPwd(String saltPassword) {
        return ARGON2_PASSWORD_ENCODER.encode(saltPassword);
    }

    /**
     * 校验密码是否匹配
     * <p>
     * 使用 Argon2 算法验证密码
     *
     * @param saltPassword   加盐后的密码
     * @param encodedPassword 加密后的密码（从数据库读取的密码）
     * @return 是否匹配
     */
    public static boolean matches(String saltPassword, String encodedPassword) {
        return ARGON2_PASSWORD_ENCODER.matches(saltPassword, encodedPassword);
    }
}
