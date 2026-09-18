package com.budaos.api.system.feign;

import com.budaos.api.system.dto.VerifyCodeDTO;
import com.budaos.api.system.dto.StaffDTO;
import com.budaos.api.system.dto.AuthLoginDTO;
import com.budaos.common.core.domain.ApiResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 登录服务 Feign 接口
 *
 * @author budaos
 */
@FeignClient(
    contextId = "loginFeignClient",
    name = "QuickBlue-system",
    path = "/login",
    fallback = AuthFeignClientFallback.class  // 降级策略
)
public interface AuthFeignClient {

    /**
     * 登录
     *
     * @param loginName 登录账号
     * @param password  密码
     * @param captchaId 验证码ID
     * @param verifyCode 验证码
     * @return 登录结果
     */
    @PostMapping
    ApiResult<AuthLoginDTO> login(
            @RequestParam("loginName") String loginName,
            @RequestParam("password") String password,
            @RequestParam(value = "captchaId", required = false) String captchaId,
            @RequestParam(value = "verifyCode", required = false) String verifyCode
    );

    /**
     * 获取登录信息
     *
     * @return 登录信息
     */
    @GetMapping("/getLoginInfo")
    ApiResult<AuthLoginDTO> getLoginInfo();

    /**
     * 登出
     *
     * @return 登出结果
     */
    @GetMapping("/logout")
    ApiResult<String> logout();

    /**
     * 获取验证码
     *
     * @return 验证码
     */
    @GetMapping("/getCaptcha")
    ApiResult<VerifyCodeDTO> getCaptcha();

    /**
     * 获取双因子登录标识
     *
     * @return 双因子登录标识
     */
    @GetMapping("/getTwoFactorLoginFlag")
    ApiResult<Boolean> getTwoFactorLoginFlag();
}
