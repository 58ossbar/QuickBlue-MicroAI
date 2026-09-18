package com.budaos.support.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 手机端登录表单
 *
 * @author budaos
 */
@Data
@Schema(description = "手机端登录表单")
public class LoginAppForm {

    @Schema(description = "登录账号")
    @NotBlank(message = "登录账号不能为空")
    @Length(max = 30, message = "登录账号最多30字符")
    private String loginName;

    @Schema(description = "密码")
    @NotBlank(message = "密码不能为空")
    private String password;

    @Schema(description = "登录终端")
    private Integer loginDevice;

    @Schema(description = "邮箱验证码")
    private String emailCode;
}
