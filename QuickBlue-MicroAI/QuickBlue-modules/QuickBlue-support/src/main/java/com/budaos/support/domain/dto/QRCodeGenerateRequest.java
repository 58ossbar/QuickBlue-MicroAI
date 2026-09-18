package com.budaos.support.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 二维码生成请求DTO
 *
 * @author QuickBlue
 * @date 2026-02-08
 */
@Data
public class QRCodeGenerateRequest {

    /**
     * 二维码内容
     */
    @NotBlank(message = "二维码内容不能为空")
    @Size(max = 4000, message = "二维码内容长度不能超过4000字符")
    private String content;

    /**
     * 二维码尺寸
     */
    @NotNull(message = "二维码尺寸不能为空")
    private Integer size;

    /**
     * 前景色（十六进制颜色码，如：#000000）
     */
    private String foregroundColor;

    /**
     * 背景色（十六进制颜色码，如：#FFFFFF）
     */
    private String backgroundColor;

    /**
     * Logo图片Base64（可选）
     */
    private String logoBase64;

    /**
     * Logo尺寸（如果提供Logo）
     */
    private Integer logoSize;

    /**
     * 是否启用加密
     */
    private Boolean encrypt = false;

    /**
     * 加密密钥
     */
    private String encryptionKey;
}
