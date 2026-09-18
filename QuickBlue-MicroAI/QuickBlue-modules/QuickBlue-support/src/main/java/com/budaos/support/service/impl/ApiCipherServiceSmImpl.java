package com.budaos.support.service.impl;

import cn.hutool.core.codec.Base64;
import cn.hutool.crypto.symmetric.SM4;
import com.budaos.support.service.ApiCipherService;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.stereotype.Service;

import java.security.Security;

/**
 * 国产 SM4 加密 和 解密
 * 1、国密SM4 要求秘钥为 128bit，转化字节为 16个字节；
 * 2、js前端使用 UCS-2 或者 UTF-16 编码，字母、数字、特殊符号等 占用1个字节；
 * 3、java中 每个 字母数字 也是占用1个字节；
 * 4、所以：前端和后端的 秘钥Key 组成为：字母、数字、特殊符号 一共16个即可
 */
@Slf4j
@Service
public class ApiCipherServiceSmImpl implements ApiCipherService {

    private static final String CHARSET = "UTF-8";
    private static final String SM4_KEY = "A3f$7cLp#2mN8!zQ";

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Override
    public String encrypt(String data) {
        try {
            // 第一步： SM4 加密
            SM4 sm4 = new SM4(hexToBytes(stringToHex(SM4_KEY)));
            String encryptHex = sm4.encryptHex(data);

            // 第二步： Base64 编码
            return Base64.encode(encryptHex.getBytes(CHARSET));

        } catch (Exception e) {
            log.error("SM4加密失败", e);
            return "";
        }
    }

    @Override
    public String decrypt(String data) {
        try {
            // 第一步： Base64 解码得到十六进制字符串
            String hexString = Base64.decodeStr(data);

            // 第二步： SM4 解密
            SM4 sm4 = new SM4(hexToBytes(stringToHex(SM4_KEY)));
            String decrypted = sm4.decryptStr(hexString);

            log.info("SM4解密成功 - 输入长度: {}, 十六进制字符串(前60字符): {}, 解密结果长度: {}",
                    data.length(), hexString.substring(0, Math.min(60, hexString.length())), decrypted.length());

            return decrypted;

        } catch (Exception e) {
            log.error("SM4解密失败 - 输入数据(前100字符): {}", data != null ? data.substring(0, Math.min(100, data.length())) : "null", e);
            return "";
        }
    }

    public static String stringToHex(String input) {
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
    public static byte[] hexToBytes(String hex) {
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
