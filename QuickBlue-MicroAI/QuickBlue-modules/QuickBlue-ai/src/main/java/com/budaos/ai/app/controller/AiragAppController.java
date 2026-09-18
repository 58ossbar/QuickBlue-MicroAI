package com.budaos.ai.app.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.budaos.ai.api.dto.AiAppDTO;
import com.budaos.ai.app.entity.AiragApp;
import com.budaos.ai.app.service.IAiragAppService;
import com.budaos.common.core.domain.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * AI应用 Controller
 */
@Slf4j
@RestController
@RequestMapping("/ai/app")
@RequiredArgsConstructor
@Tag(name = "AI应用管理", description = "AI应用管理接口")
public class AiragAppController {

    private final IAiragAppService airagAppService;

    @GetMapping("/list")
    @Operation(summary = "获取应用列表")
    public ApiResult<List<AiragApp>> list() {
        try {
            return ApiResult.ok(airagAppService.list());
        } catch (Exception e) {
            log.error("获取应用列表失败", e);
            return ApiResult.error("500", "获取应用列表失败");
        }
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询应用")
    public ApiResult<IPage<AiragApp>> page(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            AiragApp app) {
        try {
            Page<AiragApp> page = new Page<>(pageNo, pageSize);
            LambdaQueryWrapper<AiragApp> wrapper = new LambdaQueryWrapper<>();

            if (app.getName() != null && !app.getName().isEmpty()) {
                wrapper.like(AiragApp::getName, app.getName());
            }
            if (app.getStatus() != null && !app.getStatus().isEmpty()) {
                wrapper.eq(AiragApp::getStatus, app.getStatus());
            }
            if (app.getType() != null && !app.getType().isEmpty()) {
                wrapper.eq(AiragApp::getType, app.getType());
            }
            wrapper.orderByDesc(AiragApp::getCreateTime);
            return ApiResult.ok(airagAppService.page(page, wrapper));
        } catch (Exception e) {
            log.error("分页查询应用失败", e);
            return ApiResult.error("500", "分页查询应用失败");
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取应用")
    public ApiResult<AiAppDTO> getById(@PathVariable("id") String id) {
        try {
            AiragApp app = airagAppService.getById(id);
            if (app == null) {
                return ApiResult.userErrorParam("应用不存在");
            }
            AiAppDTO dto = new AiAppDTO();
            BeanUtils.copyProperties(app, dto);
            return ApiResult.ok(dto);
        } catch (Exception e) {
            log.error("获取应用详情失败, id: {}", id, e);
            return ApiResult.error("500", "获取应用详情失败");
        }
    }

    @PostMapping
    @Operation(summary = "创建应用")
    public ApiResult<Boolean> create(@Valid @RequestBody AiragApp app) {
        try {
            log.info("创建应用请求: {}", app.getName());
            boolean result = airagAppService.saveApp(app);
            return ApiResult.okMsg("创建成功");
        } catch (Exception e) {
            log.error("创建应用失败", e);
            return ApiResult.error("500", e.getMessage());
        }
    }

    @PutMapping
    @Operation(summary = "更新应用")
    public ApiResult<Boolean> update(@Valid @RequestBody AiragApp app) {
        try {
            log.info("更新应用请求: id={}, name={}", app.getId(), app.getName());
            boolean result = airagAppService.updateApp(app);
            return ApiResult.okMsg("更新成功");
        } catch (Exception e) {
            log.error("更新应用失败", e);
            return ApiResult.error("500", e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除应用")
    public ApiResult<Boolean> delete(@PathVariable("id") String id) {
        try {
            log.info("删除应用请求: id={}", id);
            boolean result = airagAppService.deleteApp(id);
            return ApiResult.okMsg("删除成功");
        } catch (Exception e) {
            log.error("删除应用失败", e);
            return ApiResult.error("500", e.getMessage());
        }
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除应用")
    public ApiResult<Boolean> batchDelete(@RequestBody List<String> ids) {
        try {
            log.info("批量删除应用请求: count={}", ids.size());
            boolean result = airagAppService.batchDelete(ids);
            return ApiResult.okMsg("批量删除成功");
        } catch (Exception e) {
            log.error("批量删除应用失败", e);
            return ApiResult.error("500", e.getMessage());
        }
    }

    @GetMapping("/exists/{id}")
    @Operation(summary = "检查应用是否存在")
    public ApiResult<Boolean> existsById(@PathVariable("id") String id) {
        try {
            return ApiResult.ok(airagAppService.getById(id) != null);
        } catch (Exception e) {
            log.error("检查应用是否存在失败", e);
            return ApiResult.ok(false);
        }
    }

    @PutMapping("/publish/{id}")
    @Operation(summary = "发布应用")
    public ApiResult<Boolean> publish(@PathVariable("id") String id) {
        try {
            log.info("发布应用请求: id={}", id);
            boolean result = airagAppService.publishApp(id);
            return ApiResult.okMsg("发布成功");
        } catch (Exception e) {
            log.error("发布应用失败", e);
            return ApiResult.error("500", e.getMessage());
        }
    }

    @PutMapping("/enable/{id}")
    @Operation(summary = "启用应用")
    public ApiResult<Boolean> enable(@PathVariable("id") String id) {
        try {
            log.info("启用应用请求: id={}", id);
            boolean result = airagAppService.enableApp(id);
            return ApiResult.okMsg("启用成功");
        } catch (Exception e) {
            log.error("启用应用失败", e);
            return ApiResult.error("500", e.getMessage());
        }
    }

    @PutMapping("/disable/{id}")
    @Operation(summary = "禁用应用")
    public ApiResult<Boolean> disable(@PathVariable("id") String id) {
        try {
            log.info("禁用应用请求: id={}", id);
            boolean result = airagAppService.disableApp(id);
            return ApiResult.okMsg("禁用成功");
        } catch (Exception e) {
            log.error("禁用应用失败", e);
            return ApiResult.error("500", e.getMessage());
        }
    }
}
