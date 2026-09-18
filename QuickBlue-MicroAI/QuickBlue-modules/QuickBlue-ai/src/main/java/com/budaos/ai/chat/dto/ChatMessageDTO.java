package com.budaos.ai.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * AI消息DTO
 */
@Data
@Schema(description = "AI消息DTO")
public class ChatMessageDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 角色 */
    @Schema(description = "角色(system/user/assistant/tool)")
    private String role;

    /** 内容 */
    @Schema(description = "内容")
    private String content;

    /** 推理内容 */
    @Schema(description = "推理内容")
    private String reasoningContent;

    /** 工具调用ID */
    @Schema(description = "工具调用ID")
    private String toolCallId;

    /** 工具名称 */
    @Schema(description = "工具名称")
    private String toolName;
}
