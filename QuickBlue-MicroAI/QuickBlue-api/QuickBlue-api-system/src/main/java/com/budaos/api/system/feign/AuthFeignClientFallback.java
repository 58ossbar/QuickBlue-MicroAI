package com.budaos.api.system.feign;

import com.budaos.api.system.dto.VerifyCodeDTO;
import com.budaos.api.system.dto.StaffDTO;
import com.budaos.api.system.dto.AuthLoginDTO;
import com.budaos.common.core.domain.ApiResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * AuthFeignClient 降级实现
 * 当 System 服务不可用时，返回降级响应
 *
 * 使用方式：
 * @FeignClient(
 *     name = "QuickBlue-system",
 *     path = "/login",
 *     fallback = AuthFeignClientFallback.class
 * )
 *
 * @author budaos
 */
@Slf4j
@Component
public class AuthFeignClientFallback implements AuthFeignClient {

    @Override
    public ApiResult<AuthLoginDTO> login(
            String loginName,
            String password,
            String captchaId,
            String verifyCode) {

        log.error("System 服务登录接口降级 - loginName: {}", loginName);

        // 返回降级响应
        return ApiResult.error(503, "系统服务暂时不可用，请稍后重试");
    }

    @Override
    public ApiResult<AuthLoginDTO> getLoginInfo() {
        log.error("System 服务获取登录信息接口降级");

        // 返回降级响应（从缓存获取，如果有的话）
        return ApiResult.error(503, "系统服务暂时不可用，请稍后重试");
    }

    @Override
    public ApiResult<String> logout() {
        log.error("System 服务登出接口降级");

        // 登出操作降级：直接返回成功
        return ApiResult.okMsg("登出成功（服务降级）");
    }

    @Override
    public ApiResult<VerifyCodeDTO> getCaptcha() {
        log.error("System 服务获取验证码接口降级");

        // 返回降级响应
        return ApiResult.error(503, "系统服务暂时不可用，请稍后重试");
    }

    @Override
    public ApiResult<Boolean> getTwoFactorLoginFlag() {
        log.error("System 服务获取双因子登录标识接口降级");

        // 返回默认值：false
        return ApiResult.ok(false);
    }
}
