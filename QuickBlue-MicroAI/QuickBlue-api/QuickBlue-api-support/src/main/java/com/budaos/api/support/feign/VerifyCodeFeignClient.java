package com.budaos.api.support.feign;

import com.budaos.api.support.dto.VerifyCodeDTO;
import com.budaos.common.core.domain.ApiResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 验证码服务 Feign 接口
 *
 * @author budaos
 */
@FeignClient(
    contextId = "captchaFeignClient",
    name = "QuickBlue-support",
    path = "/captcha"
)
public interface VerifyCodeFeignClient {

    /**
     * 生成验证码
     *
     * @return 验证码信息
     */
    @GetMapping("/generate")
    ApiResult<VerifyCodeDTO> generate();

    /**
     * 验证验证码
     *
     * @param captchaId   验证码ID
     * @param verifyCode 验证码
     * @return 验证结果
     */
    @PostMapping("/verify")
    ApiResult<Boolean> verify(
            @RequestParam("captchaId") String captchaId,
            @RequestParam("verifyCode") String verifyCode
    );
}
