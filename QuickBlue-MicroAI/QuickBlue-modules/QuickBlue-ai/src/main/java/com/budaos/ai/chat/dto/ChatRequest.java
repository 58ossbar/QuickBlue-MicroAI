package com.budaos.ai.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * AI对话请求DTO
 */
@Data
@Schema(description = "AI对话请求")
public class ChatRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 会话ID(可选，不传则创建新会话) */
    @Schema(description = "会话ID")
    private String sessionId;

    /** 应用ID */
    @Schema(description = "应用ID")
    private String appId;

    /** 模型ID(优先级高于应用配置) */
    @Schema(description = "模型ID")
    private String modelId;

    /** 用户输入内容 */
    @Schema(description = "用户输入内容", required = true)
    private String prompt;

    /** 系统提示词(可选，覆盖应用默认) */
    @Schema(description = "系统提示词")
    private String systemPrompt;

    /** 历史消息列表(可选，用于前端传入上下文) */
    @Schema(description = "历史消息列表")
    private List<ChatMessageDTO> messages;

    /** 知识库ID(可选，用于RAG) */
    @Schema(description = "知识库ID")
    private String knowledgeId;

    /** 流式输出(默认true) */
    @Schema(description = "是否流式输出")
    private Boolean stream = true;

    /** 温度参数(可选) */
    @Schema(description = "温度参数")
    private Double temperature;

    /** 最大Token数(可选) */
    @Schema(description = "最大Token数")
    private Integer maxTokens;

    /** 历史消息数限制(可选，用于上下文窗口) */
    @Schema(description = "历史消息数限制")
    private Integer historyLimit;

    /** 扩展参数(JSON格式) */
    @Schema(description = "扩展参数")
    private String extraParams;

    /** 用户ID(由系统设置) */
    @Schema(description = "用户ID", hidden = true)
    private String userId;

    /** 租户ID(由系统设置) */
    @Schema(description = "租户ID", hidden = true)
    private String tenantId;
}
