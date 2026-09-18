package com.budaos.support.controller;

import com.budaos.common.core.domain.ApiResult;
import com.budaos.support.service.SecurePasswordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 密码安全服务控制器
 *
 * @author budaos
 */
@Tag(name = "密码安全服务", description = "密码安全相关接口")
@RestController
@RequestMapping("/securityPassword")
public class SecurePasswordServiceController {

    @Resource
    private SecurePasswordService securityPasswordService;

    /**
     * 校验密码复杂度
     */
    @GetMapping("/validatePasswordComplexity")
    @Operation(summary = "校验密码复杂度")
    public ApiResult<String> validatePasswordComplexity(@RequestParam("password") String password) {
        return securityPasswordService.validatePasswordComplexity(password);
    }

    /**
     * 随机生成密码
     */
    @GetMapping("/randomPassword")
    @Operation(summary = "随机生成密码")
    public ApiResult<String> randomPassword() {
        String password = securityPasswordService.randomPassword();
        return ApiResult.ok(password);
    }

    /**
     * 检查是否需要修改密码
     */
    @GetMapping("/checkNeedChangePassword")
    @Operation(summary = "检查是否需要修改密码")
    public ApiResult<Boolean> checkNeedChangePassword(
            @RequestParam("userType") Integer userType,
            @RequestParam("userId") Long userId) {
        boolean needChange = securityPasswordService.checkNeedChangePassword(userType, userId);
        return ApiResult.ok(needChange);
    }
}
