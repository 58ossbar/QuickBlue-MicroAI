package com.budaos.business.region.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.budaos.business.region.domain.form.RegionAddForm;
import com.budaos.business.region.domain.form.RegionQueryForm;
import com.budaos.business.region.domain.form.RegionUpdateForm;
import com.budaos.business.region.domain.vo.RegionTreeVO;
import com.budaos.business.region.service.RegionService;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.common.web.annotation.AuditLog;
import com.budaos.common.web.annotation.PreventRepeatSubmit;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 区域表（树形结构） Controller
 */
@RestController
@RequestMapping("/region")
@AuditLog
@Tag(name = "区域管理")
public class RegionController {

    @Resource
    private RegionService regionService;
    @Operation(summary = "根据条件查询区域树")
    @PostMapping("/queryTree")
    @SaCheckPermission("region:query")
    public ApiResult<List<RegionTreeVO>> queryTree(@RequestBody @Valid RegionQueryForm queryForm) {
        return regionService.queryTree(queryForm);
    }

    @Operation(summary = "查询区域树形列表")
    @GetMapping("/treeList")
    @SaCheckPermission("region:querytree")
    public ApiResult<List<RegionTreeVO>> treeList() {
        return regionService.treeList();
    }

    @Operation(summary = "添加区域")
    @PostMapping("/add")
    @SaCheckPermission("region:add")
    @PreventRepeatSubmit
    public ApiResult<String> add(@RequestBody @Valid RegionAddForm addForm) {
        return regionService.add(addForm);
    }

    @Operation(summary = "更新区域")
    @PostMapping("/update")
    @SaCheckPermission("region:update")
    public ApiResult<String> update(@RequestBody @Valid RegionUpdateForm updateForm) {
        return regionService.update(updateForm);
    }

    @Operation(summary = "单个删除")
    @GetMapping("/delete/{id}")
    @SaCheckPermission("region:delete")
    public ApiResult<String> delete(@PathVariable String id) {
        return regionService.delete(id);
    }
}
