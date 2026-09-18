package com.budaos.system.controller;

import com.budaos.common.core.domain.ApiResult;
import com.budaos.system.domain.form.VerifyCodeForm;
import com.budaos.system.domain.vo.VerifyCodeVO;
import com.budaos.system.service.VerifyCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 图形验证码控制器
 *
 * @author budaos
 */
@Tag(name = "图形验证码", description = "图形验证码相关接口")
@RestController
@RequiredArgsConstructor
public class VerifyCodeController {

    private final VerifyCodeService captchaService;

    /**
     * 获取图形验证码
     */
    @Operation(summary = "获取图形验证码")
    @GetMapping("/captcha")
    public ApiResult<VerifyCodeVO> generateCaptcha() {
        return ApiResult.ok(captchaService.generateCaptcha());
    }

    /**
     * 校验图形验证码
     */
    @Operation(summary = "校验图形验证码")
    @PostMapping("/captcha/check")
    public ApiResult<String> checkCaptcha(@Valid @RequestBody VerifyCodeForm captchaForm) {
        return captchaService.checkCaptcha(captchaForm);
    }
}
