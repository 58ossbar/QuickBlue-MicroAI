package com.budaos.system.controller;

import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.system.domain.form.JobPostAddForm;
import com.budaos.system.domain.form.JobPostQueryForm;
import com.budaos.system.domain.form.JobPostUpdateForm;
import com.budaos.system.domain.vo.JobPostVO;
import com.budaos.system.service.JobPostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 职务表控制器
 *
 * @author budaos
 */
@RestController
@Tag(name = "职务管理")
@RequestMapping("/position")
public class JobPostController {

    @Resource
    private JobPostService positionService;

    @Operation(summary = "分页查询")
    @PostMapping("/queryPage")
    public ApiResult<PageResponse<JobPostVO>> queryPositionPage(@RequestBody @Valid JobPostQueryForm queryForm) {
        return ApiResult.ok(positionService.queryPage(queryForm));
    }

    @Operation(summary = "添加职务")
    @PostMapping("/add")
    public ApiResult<String> addPosition(@RequestBody @Valid JobPostAddForm addForm) {
        return positionService.add(addForm);
    }

    @Operation(summary = "更新职务")
    @PostMapping("/update")
    public ApiResult<String> updatePosition(@RequestBody @Valid JobPostUpdateForm updateForm) {
        return positionService.update(updateForm);
    }

    @Operation(summary = "批量删除职务")
    @PostMapping("/batchDelete")
    public ApiResult<String> batchDeletePosition(@RequestBody List<Long> idList) {
        return positionService.batchDelete(idList);
    }

    @Operation(summary = "删除职务")
    @GetMapping("/delete/{positionId}")
    public ApiResult<String> deletePosition(@PathVariable Long positionId) {
        return positionService.delete(positionId);
    }

    @Operation(summary = "查询所有职务")
    @GetMapping("/queryList")
    public ApiResult<List<JobPostVO>> queryListPosition() {
        return ApiResult.ok(positionService.queryList());
    }

    @Operation(summary = "查询岗位树")
    @GetMapping("/queryTree")
    public ApiResult<List<JobPostVO>> queryTreePosition() {
        return ApiResult.ok(positionService.queryTree());
    }

}
