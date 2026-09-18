package com.budaos.support.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.common.web.annotation.AuditLog;
import com.budaos.support.domain.CurrentUserImpl;
import com.budaos.support.domain.form.AttachmentQueryForm;
import com.budaos.support.domain.vo.AttachmentUploadVO;
import com.budaos.support.domain.vo.AttachmentVO;
import com.budaos.support.service.AttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件管理后台控制器
 */
@RestController
@RequestMapping("/file")
@Tag(name = "文件管理", description = "文件管理后台接口")
@AuditLog(module = "文件管理", description = "文件管理操作")
public class AdminAttachmentController {

    @Resource
    private AttachmentService fileService;

    @Operation(summary = "分页查询文件")
    @PostMapping("/queryPage")
    @SaCheckPermission("support:file:query")
    public ApiResult<PageResponse<AttachmentVO>> queryPage(@RequestBody @Valid AttachmentQueryForm queryForm) {
        return ApiResult.ok(fileService.queryPage(queryForm));
    }

    @Operation(summary = "文件上传")
    @PostMapping("/upload")
    @SaCheckPermission("support:file:upload")
    public ApiResult<AttachmentUploadVO> upload(@RequestParam("file") MultipartFile file,
                                             @RequestParam(value = "folderType", required = false, defaultValue = "1") Integer folderType) {
        CurrentUserImpl requestUser = getCurrentUser();
        return fileService.fileUpload(file, folderType, requestUser);
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
