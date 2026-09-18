package com.budaos.api.support.feign;

import com.budaos.common.core.domain.ApiResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 密码安全服务 Feign Client
 *
 * @author budaos
 */
@FeignClient(contextId = "securityPasswordServiceFeignClient", value = "QuickBlue-support")
public interface SecurePasswordServiceFeignClient {

    /**
     * 校验密码复杂度
     */
    @GetMapping("/support/securityPassword/validatePasswordComplexity")
    ApiResult<String> validatePasswordComplexity(@RequestParam("password") String password);

    /**
     * 随机生成密码
     */
    @GetMapping("/support/securityPassword/randomPassword")
    ApiResult<String> randomPassword();

    /**
     * 检查是否需要修改密码
     */
    @GetMapping("/support/securityPassword/checkNeedChangePassword")
    ApiResult<Boolean> checkNeedChangePassword(
            @RequestParam("userType") Integer userType,
            @RequestParam("userId") Long userId);
}
