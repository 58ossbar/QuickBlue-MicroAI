package com.budaos.common.security.encrypt;

import lombok.extern.slf4j.Slf4j;

/**
 * API 加密/解密工具类
 * <p>
 * 提供统一的API数据加密和解密能力，支持前端RSA加密传输，后端SM4加密存储
 *
 * @author budaos
 * @since 2026-02-19
 */
@Slf4j
public class ApiCipherUtil {

    /**
     * 加密数据
     * <p>
     * 使用SM4算法加密数据，返回Base64编码结果
     *
     * @param data 原始数据
     * @return 加密后的数据（Base64），加密失败返回空字符串
     */
    public static String encrypt(String data) {
        if (data == null || data.isEmpty()) {
            log.warn("加密数据为空");
            return "";
        }
        return Sm4Util.encrypt(data);
    }

    /**
     * 解密数据
     * <p>
     * 使用SM4算法解密Base64编码的加密数据
     *
     * @param data 加密后的数据（Base64）
     * @return 原始数据，解密失败返回空字符串
     */
    public static String decrypt(String data) {
        if (data == null || data.isEmpty()) {
            log.warn("解密数据为空");
            return "";
        }
        return Sm4Util.decrypt(data);
    }

    /**
     * 安全解密数据（带默认值）
     * <p>
     * 解密失败时返回默认值，而非空字符串
     *
     * @param data         加密后的数据（Base64）
     * @param defaultValue 解密失败时的默认值
     * @return 原始数据，解密失败返回默认值
     */
    public static String decryptSafe(String data, String defaultValue) {
        String result = decrypt(data);
        return (result != null && !result.isEmpty()) ? result : defaultValue;
    }
}
