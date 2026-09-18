package com.budaos.business.notice.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.budaos.business.notice.domain.form.AnnouncementAddForm;
import com.budaos.business.notice.domain.form.AnnouncementStaffQueryForm;
import com.budaos.business.notice.domain.form.AnnouncementQueryForm;
import com.budaos.business.notice.domain.form.AnnouncementUpdateForm;
import com.budaos.business.notice.domain.form.AnnouncementViewRecordQueryForm;
import com.budaos.business.notice.domain.vo.*;
import com.budaos.business.notice.service.AnnouncementStaffService;
import com.budaos.business.notice.service.AnnouncementService;
import com.budaos.business.notice.service.AnnouncementTypeService;
import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.common.core.util.RequestContextUtil;
import com.budaos.common.swagger.constant.OpenApiTagConst;
import com.budaos.common.web.annotation.AuditLog;
import com.budaos.common.web.annotation.PreventRepeatSubmit;
import com.budaos.common.web.util.JakartaServletUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公告、通知、新闻等等
 *
 * @author budaos
 */
@Tag(name = "通知公告")
@RequestMapping("")
@RestController
@AuditLog(module = "OA", type = "通知公告", description = "通知公告操作")
public class AnnouncementController {

    @Resource
    private AnnouncementService noticeService;

    @Resource
    private AnnouncementTypeService noticeTypeService;

    @Resource
    private AnnouncementStaffService noticeEmployeeService;

    // --------------------- 通知公告类型 -------------------------

    @Operation(summary = "通知公告类型-获取全部")
    @GetMapping("/noticeType/getAll")
    public ApiResult<List<AnnouncementTypeVO>> getAllNoticeType() {
        return ApiResult.ok(noticeTypeService.getAll());
    }

    @Operation(summary = "通知公告类型-添加")
    @GetMapping("/noticeType/add/{name}")
    public ApiResult<String> addNoticeType(@PathVariable String name) {
        return noticeTypeService.add(name);
    }

    @Operation(summary = "通知公告类型-修改")
    @GetMapping("/noticeType/update/{noticeTypeId}/{name}")
    public ApiResult<String> updateNoticeType(@PathVariable Long noticeTypeId, @PathVariable String name) {
        return noticeTypeService.update(noticeTypeId, name);
    }

    @Operation(summary = "通知公告类型-删除")
    @GetMapping("/noticeType/delete/{noticeTypeId}")
    public ApiResult<String> deleteNoticeType(@PathVariable Long noticeTypeId) {
        return noticeTypeService.delete(noticeTypeId);
    }

    // --------------------- 【管理】通知公告-------------------------


    @Operation(summary = "【管理】通知公告-分页查询")
    @PostMapping("/notice/query")
    @SaCheckPermission("oa:notice:query")
    public ApiResult<PageResponse<AnnouncementVO>> queryNotice(@RequestBody @Valid AnnouncementQueryForm queryForm) {
        return ApiResult.ok(noticeService.query(queryForm));
    }

    @Operation(summary = "【管理】通知公告-添加")
    @PostMapping("/notice/add")
    @PreventRepeatSubmit
    @SaCheckPermission("oa:notice:add")
    public ApiResult<String> addNotice(@RequestBody @Valid AnnouncementAddForm addForm) {
        addForm.setCreateUserId(RequestContextUtil.getRequestUserId());
        return noticeService.add(addForm);
    }

    @Operation(summary = "【管理】通知公告-更新")
    @PostMapping("/notice/update")
    @PreventRepeatSubmit
    @SaCheckPermission("oa:notice:update")
    public ApiResult<String> updateNotice(@RequestBody @Valid AnnouncementUpdateForm updateForm) {
        return noticeService.update(updateForm);
    }

    @Operation(summary = "【管理】通知公告-更新详情")
    @GetMapping("/notice/getUpdateVO/{noticeId}")
    @SaCheckPermission("oa:notice:update")
    public ApiResult<AnnouncementUpdateFormVO> getUpdateNoticeFormVO(@PathVariable Long noticeId) {
        return ApiResult.ok(noticeService.getUpdateFormVO(noticeId));
    }

    @Operation(summary = "【管理】通知公告-删除")
    @GetMapping("/notice/delete/{noticeId}")
    @SaCheckPermission("oa:notice:delete")
    public ApiResult<String> deleteNotice(@PathVariable Long noticeId) {
        return noticeService.delete(noticeId);
    }

    // --------------------- 【员工】查看 通知公告 -------------------------


    @Operation(summary = "【员工】通知公告-查看详情")
    @GetMapping("/notice/employee/view/{noticeId}")
    public ApiResult<AnnouncementDetailVO> viewNoticeDetail(@PathVariable Long noticeId, HttpServletRequest request) {
        return noticeEmployeeService.view(
                RequestContextUtil.getRequestUserId(),
                noticeId,
                JakartaServletUtil.getClientIP(request),
                request.getHeader("User-Agent")
        );
    }

    @Operation(summary = "【员工】通知公告-查询全部")
    @PostMapping("/notice/employee/query")
    public ApiResult<PageResponse<AnnouncementStaffVO>> queryEmployeeNotice(@RequestBody @Valid AnnouncementStaffQueryForm noticeEmployeeQueryForm) {
        return noticeEmployeeService.queryList(RequestContextUtil.getRequestUserId(), noticeEmployeeQueryForm);
    }

    @Operation(summary = "【员工】通知公告-查询 查看记录")
    @PostMapping("/notice/employee/queryViewRecord")
    public ApiResult<PageResponse<AnnouncementViewRecordVO>> queryViewRecord(@RequestBody @Valid AnnouncementViewRecordQueryForm noticeViewRecordQueryForm) {
        return ApiResult.ok(noticeEmployeeService.queryViewRecord(noticeViewRecordQueryForm));
    }
}
