package com.budaos.support.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.common.web.annotation.AuditLog;
import com.budaos.support.domain.CurrentUserImpl;
import com.budaos.support.domain.entity.HelpCenterEntity;
import com.budaos.support.domain.form.HelpCenterCatalogAddForm;
import com.budaos.support.domain.form.HelpCenterCatalogUpdateForm;
import com.budaos.support.domain.form.HelpCenterQueryForm;
import com.budaos.support.domain.form.HelpCenterViewRecordQueryForm;
import com.budaos.support.domain.vo.*;
import com.budaos.support.service.HelpCenterCatalogService;
import com.budaos.support.service.HelpCenterService;
import com.budaos.support.service.HelpCenterUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 帮助文档Controller
 *
 * @author budaos
 */
@Tag(name = "帮助文档管理")
@RestController
@RequestMapping("/helpDoc")
@AuditLog(module = "帮助文档管理", description = "帮助文档操作")
public class HelpCenterController {

    @Resource
    private HelpCenterCatalogService helpDocCatalogService;

    @Resource
    private HelpCenterUserService helpDocUserService;

    @Resource
    private com.budaos.support.service.HelpCenterService helpDocService;

    // --------------------- 帮助文档【目录】-------------------------

    @Operation(summary = "帮助文档目录-获取全部")
    @GetMapping("/helpDocCatalog/getAll")
    public ApiResult<List<HelpCenterCatalogVO>> getAll() {
        return ApiResult.ok(helpDocCatalogService.getAll());
    }

    @Operation(summary = "【管理】帮助文档目录-添加")
    @PostMapping("/helpDocCatalog/add")
    public ApiResult<String> addCatalog(@RequestBody @Valid HelpCenterCatalogAddForm helpDocCatalogAddForm) {
        return helpDocCatalogService.add(helpDocCatalogAddForm);
    }

    @Operation(summary = "【管理】帮助文档目录-更新")
    @PostMapping("/helpDocCatalog/update")
    public ApiResult<String> updateCatalog(@RequestBody @Valid HelpCenterCatalogUpdateForm updateForm) {
        return helpDocCatalogService.update(updateForm);
    }

    @Operation(summary = "【管理】帮助文档目录-删除")
    @GetMapping("/helpDocCatalog/delete/{helpDocCatalogId}")
    public ApiResult<String> deleteCatalog(@PathVariable Long helpDocCatalogId) {
        return helpDocCatalogService.delete(helpDocCatalogId);
    }

    // --------------------- 帮助文档【用户】-------------------------

    @Operation(summary = "【用户】帮助文档-查看详情")
    @GetMapping("/user/view/{helpDocId}")
    public ApiResult<HelpCenterDetailVO> view(@PathVariable Long helpDocId, HttpServletRequest request) {
        return helpDocUserService.view(
                getCurrentUser(),
                helpDocId);
    }

    @Operation(summary = "【用户】帮助文档-查询全部")
    @GetMapping("/user/queryAllHelpDocList")
    public ApiResult<List<HelpCenterVO>> queryAllHelpDocList() {
        return helpDocUserService.queryAllHelpDocList();
    }

    @Operation(summary = "【用户】帮助文档-查询查看记录")
    @PostMapping("/user/queryViewRecord")
    public ApiResult<PageResponse<HelpCenterViewRecordVO>> queryViewRecord(@RequestBody @Valid HelpCenterViewRecordQueryForm helpDocViewRecordQueryForm) {
        return ApiResult.ok(helpDocUserService.queryViewRecord(helpDocViewRecordQueryForm));
    }

    @Operation(summary = "【用户】帮助文档-获取完整数据（用于导出PDF）")
    @GetMapping("/user/getComplete")
    public ApiResult<HelpCenterCompleteVO> getComplete() {
        return ApiResult.ok(helpDocUserService.getCompleteHelpDoc());
    }

    @Operation(summary = "【管理】帮助文档-根据关联id查询")
    @GetMapping("/queryHelpDocByRelationId/{relationId}")
    public ApiResult<List<HelpCenterVO>> queryHelpDocByRelationId(@PathVariable Long relationId) {
        return ApiResult.ok(helpDocService.queryByRelationId(relationId));
    }

    @Operation(summary = "【管理】帮助文档-分页查询")
    @PostMapping("/query")
    public ApiResult<PageResponse<HelpCenterVO>> queryHelpDoc(@RequestBody HelpCenterQueryForm queryForm) {
        return ApiResult.ok(helpDocService.query(queryForm));
    }

    @Operation(summary = "【管理】帮助文档-获取详情")
    @GetMapping("/getDetail/{helpDocId}")
    public ApiResult<com.budaos.support.domain.vo.HelpCenterDetailVO> getHelpDocDetail(@PathVariable Long helpDocId) {
        return ApiResult.ok(helpDocService.getDetail(helpDocId));
    }

    @Operation(summary = "【管理】帮助文档-添加")
    @PostMapping("/add")
    public ApiResult<String> add(@RequestBody HelpCenterEntity addForm) {
        return helpDocService.add(addForm);
    }

    @Operation(summary = "【管理】帮助文档-更新")
    @PostMapping("/update")
    public ApiResult<String> updateHelpDoc(@RequestBody HelpCenterEntity updateForm) {
        return helpDocService.update(updateForm);
    }

    @Operation(summary = "【管理】帮助文档-删除")
    @GetMapping("/delete/{helpDocId}")
    public ApiResult<String> deleteHelpDoc(@PathVariable Long helpDocId) {
        return helpDocService.delete(helpDocId);
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
