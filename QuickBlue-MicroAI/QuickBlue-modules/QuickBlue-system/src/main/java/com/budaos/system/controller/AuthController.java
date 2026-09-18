package com.budaos.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.extra.servlet.JakartaServletUtil;
import com.budaos.common.core.annotation.SkipAuth;
import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.CurrentUser;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.common.core.util.RequestContextUtil;
import com.budaos.common.web.annotation.AuditLog;
import com.budaos.system.domain.form.AuthLoginForm;
import com.budaos.system.domain.form.OnlineUserQueryForm;
import com.budaos.system.domain.vo.AuthLoginVO;
import com.budaos.system.domain.vo.VerifyCodeVO;
import com.budaos.system.domain.vo.OnlineUserVO;
import com.budaos.system.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 登录控制器
 *
 * @author budaos
 */
@Tag(name = "登录认证", description = "登录认证相关接口")
@RestController
@AuditLog(module = "登录管理")
public class AuthController {

    @Resource
    private AuthService loginService;

    // ==================== 登录相关 ====================

    @SkipAuth
    @PostMapping("/login")
    @Operation(summary = "员工登录")
    public ApiResult<AuthLoginVO> login(@Valid @RequestBody AuthLoginForm loginForm, HttpServletRequest request) {
        String ip = JakartaServletUtil.getClientIP(request);
        String userAgent = JakartaServletUtil.getHeaderIgnoreCase(request, "User-Agent");
        return loginService.login(loginForm, ip, userAgent);
    }

    @GetMapping("/login/getLoginInfo")
    @Operation(summary = "获取登录信息")
    public ApiResult<AuthLoginVO> getLoginInfo() {
        return loginService.getLoginInfo();
    }

    @GetMapping("/login/logout")
    @Operation(summary = "退出登录")
    public ApiResult<String> logout() {
        CurrentUser requestUser = new CurrentUser() {
            @Override
            public Long getUserId() {
                return RequestContextUtil.getRequestUserId();
            }

            @Override
            public String getUserName() {
                return "未知用户";
            }

            @Override
            public Integer getUserType() {
                return 1; // 默认员工类型
            }

            @Override
            public String getIp() {
                return RequestContextUtil.getClientIp();
            }

            @Override
            public String getUserAgent() {
                return "Unknown";
            }
        };
        return loginService.logout(requestUser);
    }

    @SkipAuth
    @GetMapping("/login/getCaptcha")
    @Operation(summary = "获取验证码")
    public ApiResult<VerifyCodeVO> getCaptcha() {
        return loginService.getCaptcha();
    }

    @SkipAuth
    @GetMapping("/login/getTwoFactorLoginFlag")
    @Operation(summary = "获取双因子登录标识")
    public ApiResult<Boolean> getTwoFactorLoginFlag() {
        return ApiResult.ok(false);
    }

    // ==================== 在线用户管理 ====================

    @Operation(summary = "分页查询在线用户")
    @PostMapping("/online/page/query")
    @SaCheckPermission("security:onlineUser:query")
    public ApiResult<PageResponse<OnlineUserVO>> queryOnlineUserPage(@RequestBody OnlineUserQueryForm queryForm) {
        return loginService.queryOnlineUserPage(queryForm);
    }

    @Operation(summary = "获取所有在线用户")
    @GetMapping("/online/list")
    @SaCheckPermission("security:onlineUser:query")
    public ApiResult<List<OnlineUserVO>> queryAllOnlineUsers() {
        return ApiResult.ok(loginService.queryAllOnlineUsers());
    }

    @Operation(summary = "获取在线用户数量")
    @GetMapping("/online/count")
    @SaCheckPermission("security:onlineUser:query")
    public ApiResult<Long> getOnlineUserCount() {
        return ApiResult.ok(loginService.getOnlineUserCount());
    }

    @Operation(summary = "强制下线")
    @PostMapping("/online/forceLogout")
    @SaCheckPermission("security:onlineUser:forceLogout")
    public ApiResult<String> forceLogout(
            @Parameter(description = "用户ID") @RequestParam @NotNull Long userId,
            @Parameter(description = "用户类型") @RequestParam @NotNull Integer userType) {
        return loginService.forceLogout(userId, userType);
    }

    @Operation(summary = "批量强制下线")
    @PostMapping("/online/batchForceLogout")
    @SaCheckPermission("security:onlineUser:forceLogout")
    public ApiResult<String> batchForceLogout(
            @Parameter(description = "用户ID列表") @RequestParam List<Long> userIds,
            @Parameter(description = "用户类型") @RequestParam Integer userType) {
        return loginService.batchForceLogout(userIds, userType);
    }
}
