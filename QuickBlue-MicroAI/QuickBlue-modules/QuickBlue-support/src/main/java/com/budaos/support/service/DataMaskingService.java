package com.budaos.support.service;

/**
 * 数据脱敏服务
 *
 * @author budaos
 */
public interface DataMaskingService {

    /**
     * 手机号脱敏
     * 保留前3位和后4位，中间用*代替
     *
     * @param phone 手机号
     * @return 脱敏后的手机号
     */
    String maskPhone(String phone);

    /**
     * 身份证号脱敏
     * 保留前4位和后4位，中间用*代替
     *
     * @param idCard 身份证号
     * @return 脱敏后的身份证号
     */
    String maskIdCard(String idCard);

    /**
     * 银行卡号脱敏
     * 保留前4位和后4位，中间用*代替
     *
     * @param bankCard 银行卡号
     * @return 脱敏后的银行卡号
     */
    String maskBankCard(String bankCard);

    /**
     * 邮箱脱敏
     * 保留@前的首字符和@后的域名，中间用*代替
     *
     * @param email 邮箱
     * @return 脱敏后的邮箱
     */
    String maskEmail(String email);

    /**
     * 姓名脱敏
     * 保留姓，名字用*代替
     *
     * @param name 姓名
     * @return 脱敏后的姓名
     */
    String maskName(String name);

    /**
     * 地址脱敏
     * 保留省市区，详细地址用*代替
     *
     * @param address 地址
     * @return 脱敏后的地址
     */
    String maskAddress(String address);
}
