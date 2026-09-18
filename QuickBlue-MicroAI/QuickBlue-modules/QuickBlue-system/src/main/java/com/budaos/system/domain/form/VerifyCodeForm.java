package com.budaos.system.domain.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 验证码表单
 *
 * @author budaos
 */
@Data
public class VerifyCodeForm {

    /**
     * 验证码ID
     */
    @NotBlank(message = "验证码ID不能为空")
    private String captchaUuid;

    /**
     * 验证码
     */
    @NotBlank(message = "验证码不能为空")
    private String captchaCode;
}
