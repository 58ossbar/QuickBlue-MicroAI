package com.budaos.support.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.CurrentUser;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.common.core.domain.AutoValidateList;
import com.budaos.common.swagger.constant.OpenApiTagConst;
import com.budaos.common.web.annotation.AuditLog;
import com.budaos.common.web.domain.message.NotificationSendForm;
import com.budaos.common.web.domain.message.NotificationQueryForm;
import com.budaos.common.web.domain.message.NotificationVO;
import com.budaos.support.domain.CurrentUserImpl;
import com.budaos.support.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 消息 Controller
 *
 * @author budaos
 */
@Tag(name = OpenApiTagConst.Support.MESSAGE)
@RestController
@RequestMapping("/message")
@AuditLog(module = "消息管理", type = "消息操作", description = "消息管理操作")
public class NotificationController {

    @Resource
    private NotificationService messageService;

    @Operation(summary = "【管理】分页查询所有消息")
    @PostMapping("/query")
    @SaCheckPermission("support:message:query")
    public ApiResult<PageResponse<NotificationVO>> queryAdminMessage(@RequestBody @Valid NotificationQueryForm queryForm) {
        return ApiResult.ok(messageService.query(queryForm));
    }

    @Operation(summary = "【管理】发送消息")
    @PostMapping("/sendMessages")
    @SaCheckPermission("support:message:send")
    public ApiResult<String> sendMessages(@RequestBody @Valid AutoValidateList<NotificationSendForm> messageList) {
        messageService.sendMessage(messageList.getList());
        return ApiResult.ok();
    }

    @Operation(summary = "【管理】删除消息")
    @GetMapping("/delete/{messageId}")
    @SaCheckPermission("support:message:delete")
    public ApiResult<String> deleteMessage(@PathVariable Long messageId) {
        return messageService.delete(messageId);
    }

    @Operation(summary = "【员工】分页查询我的消息")
    @PostMapping("/queryMyMessage")
    public ApiResult<PageResponse<NotificationVO>> query(@RequestBody @Valid NotificationQueryForm queryForm) {
        CurrentUser user = getCurrentUser();
        if (user == null) {
            return ApiResult.userErrorParam("用户未登录");
        }

        queryForm.setSearchCount(false);
        queryForm.setReceiverUserId(user.getUserId());
        queryForm.setReceiverUserType(user.getUserType());
        return ApiResult.ok(messageService.query(queryForm));
    }

    @Operation(summary = "【员工】查询未读消息数量")
    @GetMapping("/getUnreadCount")
    public ApiResult<Long> getUnreadCount() {
        CurrentUser user = getCurrentUser();
        if (user == null) {
            return ApiResult.userErrorParam("用户未登录");
        }
        return ApiResult.ok(messageService.getUnreadCount(user.getUserType(), user.getUserId()));
    }

    @Operation(summary = "【员工】更新已读")
    @GetMapping("/read/{messageId}")
    public ApiResult<String> updateReadFlag(@PathVariable Long messageId) {
        CurrentUser user = getCurrentUser();
        if (user == null) {
            return ApiResult.userErrorParam("用户未登录");
        }

        messageService.updateReadFlag(messageId, user.getUserType(), user.getUserId());
        return ApiResult.ok();
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
