package com.budaos.support.controller;


import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.common.swagger.constant.OpenApiTagConst;
import com.budaos.support.domain.form.SystemConfigAddForm;
import com.budaos.support.domain.form.SystemConfigQueryForm;
import com.budaos.support.domain.form.SystemConfigUpdateForm;
import com.budaos.support.domain.vo.SystemConfigVO;
import com.budaos.support.service.SystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 配置
 *

 */
@Tag(name = OpenApiTagConst.Support.CONFIG)
@RestController
@RequestMapping("/config")
public class SystemConfigController {

    @Resource
    private SystemConfigService configService;

    @Operation(summary = "分页查询系统配置")
    @PostMapping("/query")
    public ApiResult<PageResponse<SystemConfigVO>> query(@RequestBody @Valid SystemConfigQueryForm queryForm) {
        return configService.queryConfigPage(queryForm);
    }

    @Operation(summary = "添加配置")
    @PostMapping("/add")
    public ApiResult<String> add(@RequestBody @Valid SystemConfigAddForm addForm) {
        return configService.add(addForm);
    }

    @Operation(summary = "更新配置")
    @PostMapping("/update")
    public ApiResult<String> update(@RequestBody @Valid SystemConfigUpdateForm updateForm) {
        return configService.updateConfig(updateForm);
    }

    @Operation(summary = "查询配置详情")
    @GetMapping("/queryByKey")
    public ApiResult<SystemConfigVO> queryByKey(@RequestParam String configKey) {
        return ApiResult.ok(configService.getConfig(configKey));
    }

}
