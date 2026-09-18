package com.budaos.support.service;

/**
 * API 加密/解密服务
 *
 * @author budaos
 */
public interface ApiCipherService {

    /**
     * 加密数据
     *
     * @param data 原始数据
     * @return 加密后的数据（Base64）
     */
    String encrypt(String data);

    /**
     * 解密数据
     *
     * @param data 加密后的数据（Base64）
     * @return 原始数据
     */
    String decrypt(String data);
}
