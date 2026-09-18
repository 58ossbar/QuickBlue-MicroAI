package com.budaos.common.security.encrypt;

import cn.hutool.core.codec.Base64;
import cn.hutool.crypto.symmetric.SM4;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;

/**
 * 国产 SM4 加密和解密工具类
 * <p>
 * 功能说明：
 * 1. 国密SM4 要求秘钥为 128bit，转化字节为 16个字节
 * 2. JS前端使用 UCS-2 或者 UTF-16 编码，字母、数字、特殊符号等占用1个字节
 * 3. Java中每个字母数字也是占用1个字节
 * 4. 前端和后端的秘钥Key组成为：字母、数字、特殊符号一共16个即可
 *
 * @author budaos
 * @since 2026-02-19
 */
@Slf4j
public class Sm4Util {

    private static final String CHARSET = "UTF-8";
    private static final String SM4_KEY = "A3f$7cLp#2mN8!zQ";

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * 加密数据
     *
     * @param data 原始数据
     * @return 加密后的数据（Base64）
     */
    public static String encrypt(String data) {
        try {
            // 第一步：SM4 加密
            SM4 sm4 = new SM4(hexToBytes(stringToHex(SM4_KEY)));
            String encryptHex = sm4.encryptHex(data);

            // 第二步：Base64 编码
            return Base64.encode(encryptHex.getBytes(CHARSET));

        } catch (Exception e) {
            log.error("SM4加密失败", e);
            return "";
        }
    }

    /**
     * 解密数据
     *
     * @param data 加密后的数据（Base64）
     * @return 原始数据
     */
    public static String decrypt(String data) {
        try {
            // 第一步：Base64 解码得到十六进制字符串
            String hexString = Base64.decodeStr(data);

            // 第二步：SM4 解密
            SM4 sm4 = new SM4(hexToBytes(stringToHex(SM4_KEY)));
            String decrypted = sm4.decryptStr(hexString);

            log.info("SM4解密成功 - 输入长度: {}, 十六进制字符串(前60字符): {}, 解密结果长度: {}",
                    data.length(), hexString.substring(0, Math.min(60, hexString.length())), decrypted.length());

            return decrypted;

        } catch (Exception e) {
            log.error("SM4解密失败 - 输入数据(前100字符): {}",
                    data != null ? data.substring(0, Math.min(100, data.length())) : "null", e);
            return "";
        }
    }

    /**
     * 字符串转16进制
     *
     * @param input 输入字符串
     * @return 16进制字符串
     */
    private static String stringToHex(String input) {
        char[] chars = input.toCharArray();
        StringBuilder hex = new StringBuilder();
        for (char c : chars) {
            hex.append(Integer.toHexString((int) c));
        }
        return hex.toString();
    }

    /**
     * 16 进制串转字节数组
     *
     * @param hex 16进制字符串
     * @return byte数组
     */
    private static byte[] hexToBytes(String hex) {
        int length = hex.length();
        byte[] result;
        if (length % 2 == 1) {
            length++;
            result = new byte[(length / 2)];
            hex = "0" + hex;
        } else {
            result = new byte[(length / 2)];
        }
        int j = 0;
        for (int i = 0; i < length; i += 2) {
            result[j] = hexToByte(hex.substring(i, i + 2));
            j++;
        }
        return result;
    }

    /**
     * 16 进制字符转字节
     *
     * @param hex 16进制字符 0x00到0xFF
     * @return byte
     */
    private static byte hexToByte(String hex) {
        return (byte) Integer.parseInt(hex, 16);
    }
}
