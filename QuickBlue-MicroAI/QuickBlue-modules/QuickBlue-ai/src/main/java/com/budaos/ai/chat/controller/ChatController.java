package com.budaos.ai.chat.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.budaos.ai.chat.dto.ChatRequest;
import com.budaos.ai.chat.dto.ChatResponse;
import com.budaos.ai.chat.service.IChatService;
import com.budaos.ai.session.entity.AiragMessage;
import com.budaos.ai.session.entity.AiragSession;
import com.budaos.ai.session.service.ISessionService;
import com.budaos.common.core.domain.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * AI对话控制器
 */
@Slf4j
@RestController
@RequestMapping("/ai/chat")
@RequiredArgsConstructor
@Tag(name = "AI对话", description = "AI对话相关接口")
public class ChatController {

    private final IChatService chatService;
    private final ISessionService sessionService;

    /**
     * 同步对话(非流式)
     */
    @PostMapping("/completions")
    @Operation(summary = "同步对话", description = "发送消息并等待完整响应")
    public ApiResult<ChatResponse> chat(@Validated @RequestBody ChatRequest request) {
        setUserInfo(request);
        ChatResponse response = chatService.chat(request);
        return ApiResult.ok(response);
    }

    /**
     * 流式对话(SSE)
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "流式对话", description = "发送消息并以SSE方式接收流式响应")
    public SseEmitter chatStream(@Validated @RequestBody ChatRequest request) {
        setUserInfo(request);
        log.info("收到流式对话请求: userId={}, prompt={}", request.getUserId(), 
                request.getPrompt() != null && request.getPrompt().length() > 50 
                        ? request.getPrompt().substring(0, 50) + "..." 
                        : request.getPrompt());
        return chatService.chatStream(request);
    }

    /**
     * 创建会话
     */
    @PostMapping("/sessions")
    @Operation(summary = "创建会话", description = "创建新的对话会话")
    public ApiResult<AiragSession> createSession(
            @Parameter(description = "应用ID") @RequestParam(required = false) String appId,
            @Parameter(description = "会话标题") @RequestParam(required = false) String title) {
        String userId = getCurrentUserId();
        AiragSession session = sessionService.createSession(appId, userId, title);
        return ApiResult.ok(session);
    }

    /**
     * 获取会话列表
     */
    @GetMapping("/sessions")
    @Operation(summary = "获取会话列表", description = "获取当前用户的会话列表")
    public ApiResult<?> getSessions(
            @Parameter(description = "应用ID") @RequestParam(required = false) String appId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") int pageSize) {
        String userId = getCurrentUserId();
        return ApiResult.ok(sessionService.getUserSessions(userId, appId, pageNum, pageSize));
    }

    /**
     * 获取会话详情
     */
    @GetMapping("/sessions/{sessionId}")
    @Operation(summary = "获取会话详情", description = "获取指定会话的详细信息")
    public ApiResult<AiragSession> getSession(
            @Parameter(description = "会话ID") @PathVariable String sessionId) {
        return ApiResult.ok(sessionService.getSession(sessionId));
    }

    /**
     * 更新会话标题
     */
    @PutMapping("/sessions/{sessionId}/title")
    @Operation(summary = "更新会话标题", description = "更新指定会话的标题")
    public ApiResult<Void> updateTitle(
            @Parameter(description = "会话ID") @PathVariable String sessionId,
            @Parameter(description = "新标题") @RequestParam String title) {
        sessionService.updateTitle(sessionId, title);
        return ApiResult.ok();
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/sessions/{sessionId}")
    @Operation(summary = "删除会话", description = "删除指定会话及其消息")
    public ApiResult<Void> deleteSession(
            @Parameter(description = "会话ID") @PathVariable String sessionId) {
        sessionService.deleteSession(sessionId);
        return ApiResult.ok();
    }

    /**
     * 获取会话消息列表
     */
    @GetMapping("/sessions/{sessionId}/messages")
    @Operation(summary = "获取会话消息", description = "获取指定会话的所有消息")
    public ApiResult<List<AiragMessage>> getSessionMessages(
            @Parameter(description = "会话ID") @PathVariable String sessionId) {
        return ApiResult.ok(sessionService.getSessionMessages(sessionId));
    }

    /**
     * 清空会话消息
     */
    @DeleteMapping("/sessions/{sessionId}/messages")
    @Operation(summary = "清空会话消息", description = "清空指定会话的所有消息")
    public ApiResult<Void> clearMessages(
            @Parameter(description = "会话ID") @PathVariable String sessionId) {
        sessionService.clearMessages(sessionId);
        return ApiResult.ok();
    }

    /**
     * 设置用户信息
     */
    private void setUserInfo(ChatRequest request) {
        if (request.getUserId() == null) {
            request.setUserId(getCurrentUserId());
        }
        // 租户ID可以从StpUtil的session或请求头获取
        // request.setTenantId(getCurrentTenantId());
    }

    /**
     * 获取当前用户ID
     */
    private String getCurrentUserId() {
        try {
            if (StpUtil.isLogin()) {
                return StpUtil.getLoginIdAsString();
            }
        } catch (Exception e) {
            log.debug("获取用户ID失败", e);
        }
        return "anonymous";
    }
}
