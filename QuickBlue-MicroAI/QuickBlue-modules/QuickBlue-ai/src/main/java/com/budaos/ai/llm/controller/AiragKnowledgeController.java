package com.budaos.ai.llm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.budaos.ai.api.dto.AiKnowledgeDTO;
import com.budaos.ai.llm.entity.AiragKnowledge;
import com.budaos.ai.llm.service.IAiragKnowledgeService;
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
 * AI知识库 Controller
 */
@Slf4j
@RestController
@RequestMapping("/ai/knowledge")
@RequiredArgsConstructor
@Tag(name = "AI知识库管理", description = "AI知识库接口")
public class AiragKnowledgeController {

    private final IAiragKnowledgeService knowledgeService;

    @GetMapping("/list")
    @Operation(summary = "获取知识库列表")
    public ApiResult<List<AiragKnowledge>> list() {
        try {
            return ApiResult.ok(knowledgeService.listEnabled());
        } catch (Exception e) {
            log.error("获取知识库列表失败", e);
            return ApiResult.error("500", "获取知识库列表失败");
        }
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询知识库")
    public ApiResult<IPage<AiragKnowledge>> page(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            AiragKnowledge knowledge) {
        try {
            Page<AiragKnowledge> page = new Page<>(pageNo, pageSize);
            LambdaQueryWrapper<AiragKnowledge> wrapper = new LambdaQueryWrapper<>();

            if (knowledge.getName() != null && !knowledge.getName().isEmpty()) {
                wrapper.like(AiragKnowledge::getName, knowledge.getName());
            }
            if (knowledge.getType() != null && !knowledge.getType().isEmpty()) {
                wrapper.eq(AiragKnowledge::getType, knowledge.getType());
            }
            if (knowledge.getStatus() != null && !knowledge.getStatus().isEmpty()) {
                wrapper.eq(AiragKnowledge::getStatus, knowledge.getStatus());
            }
            wrapper.orderByDesc(AiragKnowledge::getCreateTime);
            return ApiResult.ok(knowledgeService.page(page, wrapper));
        } catch (Exception e) {
            log.error("分页查询知识库失败", e);
            return ApiResult.error("500", "分页查询知识库失败");
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取知识库")
    public ApiResult<AiKnowledgeDTO> getById(@PathVariable("id") String id) {
        try {
            AiragKnowledge knowledge = knowledgeService.getById(id);
            if (knowledge == null) {
                return ApiResult.userErrorParam("知识库不存在");
            }
            AiKnowledgeDTO dto = new AiKnowledgeDTO();
            BeanUtils.copyProperties(knowledge, dto);
            return ApiResult.ok(dto);
        } catch (Exception e) {
            log.error("获取知识库详情失败, id: {}", id, e);
            return ApiResult.error("500", "获取知识库详情失败");
        }
    }

    @PostMapping
    @Operation(summary = "创建知识库")
    public ApiResult<Boolean> create(@Valid @RequestBody AiragKnowledge knowledge) {
        try {
            log.info("创建知识库请求: {}", knowledge.getName());
            boolean result = knowledgeService.save(knowledge);
            return ApiResult.okMsg("创建成功");
        } catch (Exception e) {
            log.error("创建知识库失败", e);
            return ApiResult.error("500", e.getMessage());
        }
    }

    @PutMapping
    @Operation(summary = "更新知识库")
    public ApiResult<Boolean> update(@Valid @RequestBody AiragKnowledge knowledge) {
        try {
            log.info("更新知识库请求: id={}, name={}", knowledge.getId(), knowledge.getName());
            boolean result = knowledgeService.updateById(knowledge);
            return ApiResult.okMsg("更新成功");
        } catch (Exception e) {
            log.error("更新知识库失败", e);
            return ApiResult.error("500", e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除知识库")
    public ApiResult<Boolean> delete(@PathVariable("id") String id) {
        try {
            log.info("删除知识库请求: id={}", id);
            boolean result = knowledgeService.removeById(id);
            return ApiResult.okMsg("删除成功");
        } catch (Exception e) {
            log.error("删除知识库失败", e);
            return ApiResult.error("500", e.getMessage());
        }
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除知识库")
    public ApiResult<Boolean> batchDelete(@RequestBody List<String> ids) {
        try {
            log.info("批量删除知识库请求: count={}", ids.size());
            boolean result = knowledgeService.removeByIds(ids);
            return ApiResult.okMsg("批量删除成功");
        } catch (Exception e) {
            log.error("批量删除知识库失败", e);
            return ApiResult.error("500", e.getMessage());
        }
    }

    @GetMapping("/exists/{id}")
    @Operation(summary = "检查知识库是否存在")
    public ApiResult<Boolean> existsById(@PathVariable("id") String id) {
        try {
            return ApiResult.ok(knowledgeService.getById(id) != null);
        } catch (Exception e) {
            log.error("检查知识库是否存在失败", e);
            return ApiResult.ok(false);
        }
    }

    @PutMapping("/enable/{id}")
    @Operation(summary = "启用知识库")
    public ApiResult<Boolean> enable(@PathVariable("id") String id) {
        try {
            log.info("启用知识库请求: id={}", id);
            boolean result = knowledgeService.enableKnowledge(id);
            return ApiResult.okMsg("启用成功");
        } catch (Exception e) {
            log.error("启用知识库失败", e);
            return ApiResult.error("500", e.getMessage());
        }
    }

    @PutMapping("/disable/{id}")
    @Operation(summary = "禁用知识库")
    public ApiResult<Boolean> disable(@PathVariable("id") String id) {
        try {
            log.info("禁用知识库请求: id={}", id);
            boolean result = knowledgeService.disableKnowledge(id);
            return ApiResult.okMsg("禁用成功");
        } catch (Exception e) {
            log.error("禁用知识库失败", e);
            return ApiResult.error("500", e.getMessage());
        }
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "根据类型获取知识库")
    public ApiResult<List<AiragKnowledge>> listByType(@PathVariable("type") String type) {
        try {
            return ApiResult.ok(knowledgeService.listByType(type));
        } catch (Exception e) {
            log.error("根据类型获取知识库失败", e);
            return ApiResult.error("500", "获取知识库失败");
        }
    }
}
