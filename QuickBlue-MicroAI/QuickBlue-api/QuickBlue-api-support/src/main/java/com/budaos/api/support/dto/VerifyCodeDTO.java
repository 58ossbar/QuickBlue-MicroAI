package com.budaos.api.support.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 验证码DTO
 *
 * @author budaos
 */
@Data
public class VerifyCodeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 验证码ID
     */
    private String captchaId;

    /**
     * 验证码图片（Base64）
     */
    private String captchaImage;

    /**
     * 验证码字符串（开发环境返回）
     */
    private String captchaCode;
}
