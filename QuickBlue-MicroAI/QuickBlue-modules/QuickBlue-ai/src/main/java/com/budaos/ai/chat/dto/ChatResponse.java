package com.budaos.ai.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * AI对话响应DTO
 */
@Data
@Schema(description = "AI对话响应")
public class ChatResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 会话ID */
    @Schema(description = "会话ID")
    private String sessionId;

    /** 消息ID */
    @Schema(description = "消息ID")
    private String messageId;

    /** 角色 */
    @Schema(description = "角色")
    private String role;

    /** 内容 */
    @Schema(description = "内容")
    private String content;

    /** 推理内容 */
    @Schema(description = "推理内容")
    private String reasoningContent;

    /** 模型名称 */
    @Schema(description = "模型名称")
    private String model;

    /** 输入Token数 */
    @Schema(description = "输入Token数")
    private Integer inputTokens;

    /** 输出Token数 */
    @Schema(description = "输出Token数")
    private Integer outputTokens;

    /** 总Token数 */
    @Schema(description = "总Token数")
    private Integer totalTokens;

    /** 响应时间(毫秒) */
    @Schema(description = "响应时间")
    private Long responseTime;

    /** 是否完成 */
    @Schema(description = "是否完成")
    private Boolean finished;

    /** 是否成功 */
    @Schema(description = "是否成功")
    private Boolean success;

    /** 错误信息 */
    @Schema(description = "错误信息")
    private String errorMsg;

    /**
     * 创建成功响应
     */
    public static ChatResponse success(String sessionId, String content) {
        ChatResponse response = new ChatResponse();
        response.setSessionId(sessionId);
        response.setRole("assistant");
        response.setContent(content);
        response.setFinished(true);
        response.setSuccess(true);
        return response;
    }

    /**
     * 创建流式响应
     */
    public static ChatResponse streaming(String sessionId, String content) {
        ChatResponse response = new ChatResponse();
        response.setSessionId(sessionId);
        response.setRole("assistant");
        response.setContent(content);
        response.setFinished(false);
        response.setSuccess(true);
        return response;
    }

    /**
     * 创建错误响应
     */
    public static ChatResponse error(String sessionId, String errorMsg) {
        ChatResponse response = new ChatResponse();
        response.setSessionId(sessionId);
        response.setFinished(true);
        response.setSuccess(false);
        response.setErrorMsg(errorMsg);
        return response;
    }
}
