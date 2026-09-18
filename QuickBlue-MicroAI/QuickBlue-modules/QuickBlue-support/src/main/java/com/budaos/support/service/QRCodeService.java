package com.budaos.support.service;

import com.budaos.support.domain.dto.QRCodeGenerateRequest;
import com.budaos.support.domain.dto.QRCodeResponse;

/**
 * 二维码服务
 *
 * @author QuickBlue
 * @date 2026-02-08
 */
public interface QRCodeService {

    /**
     * 生成二维码
     *
     * @param request 生成请求
     * @return 二维码响应
     */
    QRCodeResponse generateQRCode(QRCodeGenerateRequest request);

    /**
     * 解析二维码
     *
     * @param base64Image Base64编码的二维码图片
     * @return 解析出的内容
     */
    String decodeQRCode(String base64Image);
}
