package com.budaos.support.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 二维码生成响应DTO
 *
 * @author QuickBlue
 * @date 2026-02-08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QRCodeResponse {

    /**
     * 二维码Base64数据
     */
    private String qrCodeBase64;

    /**
     * 生成耗时（毫秒）
     */
    private Long generateTimeMs;

    /**
     * 文件大小（字节）
     */
    private Integer fileSize;

    /**
     * 图片格式
     */
    private String imageFormat;
}
