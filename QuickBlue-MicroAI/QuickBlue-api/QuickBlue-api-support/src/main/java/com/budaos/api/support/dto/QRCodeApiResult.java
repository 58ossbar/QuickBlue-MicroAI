package com.budaos.api.support.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 二维码响应DTO
 *
 * @author budaos
 */
@Data
public class QRCodeApiResult implements Serializable {

    private static final long serialVersionUID = 1L;

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
