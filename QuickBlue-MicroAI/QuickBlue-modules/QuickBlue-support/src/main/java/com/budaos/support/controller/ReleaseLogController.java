package com.budaos.support.controller;

import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.common.web.annotation.AuditLog;
import com.budaos.support.domain.form.ReleaseLogAddForm;
import com.budaos.support.domain.form.ReleaseLogQueryForm;
import com.budaos.support.domain.form.ReleaseLogUpdateForm;
import com.budaos.support.domain.vo.ReleaseLogVO;
import com.budaos.support.service.ReleaseLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统更新日志Controller
 *
 * @author budaos
 */
@Tag(name = "系统更新日志管理")
@RestController
@RequestMapping("/changeLog")
@AuditLog(module = "系统更新日志管理", description = "系统更新日志操作")
public class ReleaseLogController {

    @Resource
    private ReleaseLogService changeLogService;

    @Operation(summary = "系统更新日志-分页查询")
    @PostMapping("/queryPage")
    public ApiResult<PageResponse<ReleaseLogVO>> queryPage(@RequestBody ReleaseLogQueryForm queryForm) {
        return ApiResult.ok(changeLogService.query(queryForm));
    }

    @Operation(summary = "【管理】系统更新日志-添加")
    @PostMapping("/add")
    public ApiResult<String> add(@RequestBody @Valid ReleaseLogAddForm addForm) {
        return changeLogService.add(addForm);
    }

    @Operation(summary = "【管理】系统更新日志-更新")
    @PostMapping("/update")
    public ApiResult<String> update(@RequestBody @Valid ReleaseLogUpdateForm updateForm) {
        return changeLogService.update(updateForm);
    }

    @Operation(summary = "【管理】系统更新日志-删除")
    @GetMapping("/delete/{changeLogId}")
    public ApiResult<String> delete(@PathVariable Long changeLogId) {
        return changeLogService.delete(changeLogId);
    }

    @Operation(summary = "【管理】系统更新日志-批量删除")
    @PostMapping("/batchDelete")
    public ApiResult<String> batchDelete(@RequestBody List<Long> changeLogIdList) {
        return changeLogService.batchDelete(changeLogIdList);
    }
}
