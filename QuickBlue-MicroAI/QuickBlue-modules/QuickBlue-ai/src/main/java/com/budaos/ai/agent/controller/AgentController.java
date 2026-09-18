package com.budaos.ai.agent.controller;

import com.budaos.ai.agent.dto.AgentExecuteRequest;
import com.budaos.ai.agent.dto.AgentExecuteResponse;
import com.budaos.ai.agent.service.IAgentService;
import com.budaos.ai.llm.entity.AiragAgent;
import com.budaos.ai.llm.entity.AiragKnowledge;
import com.budaos.ai.llm.mapper.AiragAgentMapper;
import com.budaos.ai.llm.service.IAiragKnowledgeService;
import com.budaos.common.core.domain.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * Agent控制器
 */
@Slf4j
@RestController
@RequestMapping("/ai/agent")
@RequiredArgsConstructor
@Tag(name = "智能体管理", description = "AI智能体相关接口")
public class AgentController {

    private final IAgentService agentService;
    private final AiragAgentMapper agentMapper;
    private final IAiragKnowledgeService knowledgeService;

    @Operation(summary = "查询Agent列表")
    @GetMapping("/list")
    public ApiResult<List<AiragAgent>> list(@RequestParam(required = false) String status) {
        try {
            log.info("查询Agent列表, 状态: {}", status);

            // TODO: 实现查询逻辑
            return ApiResult.ok();
        } catch (Exception e) {
            log.error("查询Agent列表失败", e);
            return ApiResult.error("500", "查询Agent列表失败");
        }
    }

    @Operation(summary = "根据ID查询Agent")
    @GetMapping("/{id}")
    public ApiResult<AiragAgent> getById(@PathVariable String id) {
        try {
            log.info("查询Agent详情, ID: {}", id);

            AiragAgent agent = agentMapper.selectById(id);
            if (agent == null) {
                return ApiResult.error("404", "Agent不存在");
            }

            return ApiResult.ok(agent);
        } catch (Exception e) {
            log.error("查询Agent详情失败", e);
            return ApiResult.error("500", "查询Agent详情失败");
        }
    }

    @Operation(summary = "创建Agent")
    @PostMapping
    public ApiResult<AiragAgent> create(@RequestBody AiragAgent agent) {
        try {
            log.info("创建Agent, 名称: {}", agent.getName());

            // TODO: 实现创建逻辑

            return ApiResult.okMsg("创建成功");
        } catch (Exception e) {
            log.error("创建Agent失败", e);
            return ApiResult.error("500", "创建Agent失败");
        }
    }

    @Operation(summary = "更新Agent")
    @PutMapping
    public ApiResult<Void> update(@RequestBody AiragAgent agent) {
        try {
            log.info("更新Agent, ID: {}", agent.getId());

            // TODO: 实现更新逻辑

            return ApiResult.okMsg("更新成功");
        } catch (Exception e) {
            log.error("更新Agent失败", e);
            return ApiResult.error("500", "更新Agent失败");
        }
    }

    @Operation(summary = "删除Agent")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable String id) {
        try {
            log.info("删除Agent, ID: {}", id);

            // TODO: 实现删除逻辑

            return ApiResult.okMsg("删除成功");
        } catch (Exception e) {
            log.error("删除Agent失败", e);
            return ApiResult.error("500", "删除Agent失败");
        }
    }

    @Operation(summary = "发布Agent")
    @PostMapping("/{id}/publish")
    public ApiResult<Void> publish(@PathVariable String id) {
        try {
            log.info("发布Agent, ID: {}", id);

            boolean result = agentService.publishAgent(id);
            if (result) {
                return ApiResult.okMsg("发布成功");
            } else {
                return ApiResult.error("500", "发布失败");
            }
        } catch (Exception e) {
            log.error("发布Agent失败", e);
            return ApiResult.error("500", "发布失败");
        }
    }

    @Operation(summary = "启用Agent")
    @PostMapping("/{id}/enable")
    public ApiResult<Void> enable(@PathVariable String id) {
        try {
            log.info("启用Agent, ID: {}", id);

            boolean result = agentService.enableAgent(id);
            if (result) {
                return ApiResult.okMsg("启用成功");
            } else {
                return ApiResult.error("500", "启用失败");
            }
        } catch (Exception e) {
            log.error("启用Agent失败", e);
            return ApiResult.error("500", "启用失败");
        }
    }

    @Operation(summary = "禁用Agent")
    @PostMapping("/{id}/disable")
    public ApiResult<Void> disable(@PathVariable String id) {
        try {
            log.info("禁用Agent, ID: {}", id);

            boolean result = agentService.disableAgent(id);
            if (result) {
                return ApiResult.okMsg("禁用成功");
            } else {
                return ApiResult.error("500", "禁用失败");
            }
        } catch (Exception e) {
            log.error("禁用Agent失败", e);
            return ApiResult.error("500", "禁用失败");
        }
    }

    @Operation(summary = "执行Agent（同步）")
    @PostMapping("/execute")
    public ApiResult<AgentExecuteResponse> execute(@RequestBody AgentExecuteRequest request) {
        try {
            log.info("执行Agent, Agent ID: {}, Input: {}", request.getAgentId(), request.getInput());

            AgentExecuteResponse response = agentService.execute(request);
            return ApiResult.ok(response);
        } catch (Exception e) {
            log.error("执行Agent失败", e);
            return ApiResult.error("500", "执行Agent失败: " + e.getMessage());
        }
    }

    @Operation(summary = "执行Agent（流式）")
    @PostMapping("/execute/stream")
    public SseEmitter executeStream(@RequestBody AgentExecuteRequest request) {
        try {
            log.info("流式执行Agent, Agent ID: {}, Input: {}", request.getAgentId(), request.getInput());

            return agentService.executeStream(request);
        } catch (Exception e) {
            log.error("流式执行Agent失败", e);
            throw new RuntimeException("流式执行Agent失败", e);
        }
    }
}
