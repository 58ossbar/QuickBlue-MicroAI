package com.budaos.support.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.common.web.annotation.AuditLog;
import com.budaos.support.domain.CurrentUserImpl;
import com.budaos.support.domain.form.SuggestionAddForm;
import com.budaos.support.domain.form.SuggestionQueryForm;
import com.budaos.support.domain.vo.SuggestionVO;
import com.budaos.support.service.SuggestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 反馈Controller
 *
 * @author budaos
 */
@Tag(name = "反馈管理")
@RestController
@RequestMapping("/feedback")
@AuditLog(module = "反馈管理", description = "反馈操作")
public class SuggestionController {

    @Resource
    private SuggestionService feedbackService;

    @Operation(summary = "意见反馈-分页查询")
    @PostMapping("/query")
    public ApiResult<PageResponse<SuggestionVO>> query(@RequestBody @Valid SuggestionQueryForm queryForm) {
        return feedbackService.query(queryForm);
    }

    @Operation(summary = "意见反馈-新增")
    @PostMapping("/add")
    public ApiResult<String> add(@RequestBody @Valid SuggestionAddForm addForm) {
        CurrentUserImpl employee = getCurrentUser();
        return feedbackService.add(addForm, employee);
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
