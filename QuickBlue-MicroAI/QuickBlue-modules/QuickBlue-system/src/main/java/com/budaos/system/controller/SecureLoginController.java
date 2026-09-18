package com.budaos.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.system.domain.form.LoginAttemptQueryForm;
import com.budaos.system.domain.vo.LoginAttemptVO;
import com.budaos.system.service.SecureLoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 登录安全控制器
 *
 * @author budaos
 */
@Tag(name = "登录安全管理", description = "登录失败锁定相关接口")
@RestController
@RequestMapping("/protect/loginFail")
@RequiredArgsConstructor
public class SecureLoginController {

    private final SecureLoginService securityLoginService;

    /**
     * 分页查询登录失败记录
     */
    @Operation(summary = "分页查询登录失败记录")
    @PostMapping("/queryPage")
    public ApiResult<PageResponse<LoginAttemptVO>> queryPage(@RequestBody LoginAttemptQueryForm queryForm) {
        return ApiResult.ok(securityLoginService.queryPage(queryForm));
    }

    /**
     * 批量删除登录失败记录
     */
    @Operation(summary = "批量删除登录失败记录")
    @PostMapping("/batchDelete")
    public ApiResult<String> batchDelete(@RequestBody List<Long> idList) {
        return securityLoginService.batchDelete(idList);
    }
}
