package com.budaos.support.service;

import com.budaos.support.constant.SerialCodeIdEnum;

import java.util.List;

/**
 * 单据序列号服务
 *
 * @author QuickBlue
 * @date 2026-02-08
 */
public interface SerialCodeService {

    /**
     * 生成序列号
     *
     * @param serialNumberIdEnum 序列号枚举
     * @return 序列号
     */
    String generate(SerialCodeIdEnum serialNumberIdEnum);

    /**
     * 批量生成序列号
     *
     * @param serialNumberIdEnum 序列号枚举
     * @param count              数量
     * @return 序列号列表
     */
    List<String> generate(SerialCodeIdEnum serialNumberIdEnum, int count);
}
