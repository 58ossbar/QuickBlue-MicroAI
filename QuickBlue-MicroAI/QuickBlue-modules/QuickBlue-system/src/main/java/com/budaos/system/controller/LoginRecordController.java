package com.budaos.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.system.domain.form.LoginRecordQueryForm;
import com.budaos.system.domain.vo.LoginRecordVO;
import com.budaos.system.service.LoginRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 登录日志控制器
 *
 * @author budaos
 */
@Tag(name = "登录日志管理", description = "登录日志管理相关接口")
@RestController
@RequestMapping("/loginLog")
@RequiredArgsConstructor
public class LoginRecordController {

    private final LoginRecordService loginLogService;

    /**
     * 分页查询登录日志
     */
    @Operation(summary = "分页查询登录日志")
    @PostMapping("/page/query")
    @SaCheckPermission("support:loginLog:query")
    public ApiResult<PageResponse<LoginRecordVO>> queryLoginLogByPage(@RequestBody LoginRecordQueryForm queryForm) {
        return loginLogService.queryByPage(queryForm);
    }

    /**
     * 分页查询当前登录人登录日志
     */
    @Operation(summary = "分页查询当前登录人登录日志")
    @PostMapping("/page/query/login")
    public ApiResult<PageResponse<LoginRecordVO>> queryByPageLogin(@RequestBody LoginRecordQueryForm queryForm) {
        return loginLogService.queryByPageLogin(queryForm);
    }
}
