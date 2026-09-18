package com.budaos.ai.llm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.budaos.ai.api.dto.AiModelDTO;
import com.budaos.ai.llm.entity.AiragModel;
import com.budaos.ai.llm.service.IAiragModelService;
import com.budaos.common.core.domain.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI模型 Controller
 */
@Slf4j
@RestController
@RequestMapping("/ai/model")
@RequiredArgsConstructor
@Tag(name = "AI模型管理", description = "AI模型配置接口")
public class AiragModelController {

    private final IAiragModelService airagModelService;

    @GetMapping("/list")
    @Operation(summary = "获取模型列表")
    public ApiResult<List<AiragModel>> list() {
        return ApiResult.ok(airagModelService.listActivated());
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询模型")
    public ApiResult<IPage<AiragModel>> page(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            AiragModel model) {
        Page<AiragModel> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<AiragModel> wrapper = new LambdaQueryWrapper<>();
        if (model.getName() != null && !model.getName().isEmpty()) {
            wrapper.like(AiragModel::getName, model.getName());
        }
        if (model.getProvider() != null && !model.getProvider().isEmpty()) {
            wrapper.eq(AiragModel::getProvider, model.getProvider());
        }
        if (model.getModelType() != null && !model.getModelType().isEmpty()) {
            wrapper.eq(AiragModel::getModelType, model.getModelType());
        }
        wrapper.orderByDesc(AiragModel::getCreateTime);
        return ApiResult.ok(airagModelService.page(page, wrapper));
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取模型")
    public ApiResult<AiModelDTO> getById(@PathVariable("id") String id) {
        AiragModel model = airagModelService.getById(id);
        if (model == null) {
            return ApiResult.userErrorParam("模型不存在");
        }
        AiModelDTO dto = new AiModelDTO();
        BeanUtils.copyProperties(model, dto);
        return ApiResult.ok(dto);
    }

    @GetMapping("/default")
    @Operation(summary = "获取默认模型")
    public ApiResult<AiModelDTO> getDefaultModel() {
        AiragModel model = airagModelService.getDefaultModel();
        if (model == null) {
            return ApiResult.userErrorParam("未配置默认模型");
        }
        AiModelDTO dto = new AiModelDTO();
        BeanUtils.copyProperties(model, dto);
        return ApiResult.ok(dto);
    }

    @PostMapping
    @Operation(summary = "创建模型")
    public ApiResult<Boolean> create(@RequestBody AiragModel model) {
        return ApiResult.ok(airagModelService.save(model));
    }

    @PutMapping
    @Operation(summary = "更新模型")
    public ApiResult<Boolean> update(@RequestBody AiragModel model) {
        return ApiResult.ok(airagModelService.updateById(model));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除模型")
    public ApiResult<Boolean> delete(@PathVariable("id") String id) {
        return ApiResult.ok(airagModelService.removeById(id));
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除模型")
    public ApiResult<Boolean> batchDelete(@RequestBody List<String> ids) {
        return ApiResult.ok(airagModelService.removeBatchByIds(ids));
    }

    @PutMapping("/activate/{id}")
    @Operation(summary = "激活模型")
    public ApiResult<Boolean> activate(@PathVariable("id") String id) {
        AiragModel model = airagModelService.getById(id);
        if (model == null) {
            return ApiResult.userErrorParam("模型不存在");
        }
        model.setActivateFlag(1);
        return ApiResult.ok(airagModelService.updateById(model));
    }
}
