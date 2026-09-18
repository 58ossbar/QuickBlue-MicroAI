package com.budaos.support.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.support.domain.CurrentUserImpl;
import com.budaos.support.service.ColumnSettingService;
import com.budaos.support.domain.ColumnSettingUpdateForm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 表格列配置Controller
 *
 * @author budaos
 */
@Tag(name = "表格列配置")
@RestController
@RequestMapping("/tableColumn")
public class ColumnSettingController {

    @Resource
    private ColumnSettingService tableColumnService;

    /**
     * 查询表格列配置
     */
    @Operation(summary = "查询表格列配置")
    @GetMapping("/getColumns/{tableId}")
    public ApiResult<String> getColumns(@PathVariable Integer tableId) {
        CurrentUserImpl requestUser = getCurrentUser();
        if (requestUser == null) {
            // 未登录或无法获取用户信息时返回空
            return ApiResult.ok(null);
        }
        String columns = tableColumnService.getTableColumns(requestUser, tableId);
        return ApiResult.ok(columns);
    }

    /**
     * 修改表格列配置
     */
    @Operation(summary = "修改表格列配置")
    @PostMapping("/update")
    public ApiResult<String> updateTableColumn(@RequestBody @Valid ColumnSettingUpdateForm updateForm) {
        CurrentUserImpl requestUser = getCurrentUser();
        if (requestUser == null) {
            // 未登录或无法获取用户信息时返回错误
            return ApiResult.userErrorParam("用户未登录");
        }
        com.budaos.common.core.domain.ApiResult<String> result = tableColumnService.updateTableColumns(requestUser, updateForm);
        if (result.getOk()) {
            return ApiResult.ok(result.getData());
        } else {
            return ApiResult.userErrorParam(result.getMsg());
        }
    }

    /**
     * 删除表格列配置
     */
    @Operation(summary = "删除表格列配置")
    @GetMapping("/delete/{tableId}")
    public ApiResult<String> deleteTableColumn(@PathVariable Integer tableId) {
        CurrentUserImpl requestUser = getCurrentUser();
        if (requestUser == null) {
            // 未登录或无法获取用户信息时返回错误
            return ApiResult.userErrorParam("用户未登录");
        }
        com.budaos.common.core.domain.ApiResult<String> result = tableColumnService.deleteTableColumn(requestUser, tableId);
        if (result.getOk()) {
            return ApiResult.ok(result.getData());
        } else {
            return ApiResult.userErrorParam(result.getMsg());
        }
    }

    /**
     * 获取当前登录用户
     */
    private CurrentUserImpl getCurrentUser() {
        if (!StpUtil.isLogin()) {
            return null;
        }
        CurrentUserImpl user = new CurrentUserImpl();
        // 登录ID格式为 "userType:userId"，需要解析
        String loginId = StpUtil.getLoginIdAsString();
        if (loginId != null && loginId.contains(":")) {
            String[] parts = loginId.split(":");
            user.setUserType(Integer.parseInt(parts[0]));
            user.setUserId(Long.parseLong(parts[1]));
        }
        user.setUserName(StpUtil.getSession().getString("userName"));
        return user;
    }
}
