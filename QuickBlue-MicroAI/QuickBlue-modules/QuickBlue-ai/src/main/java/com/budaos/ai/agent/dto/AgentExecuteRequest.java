package com.budaos.ai.agent.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Agent执行请求DTO
 */
@Data
@Schema(description = "Agent执行请求")
public class AgentExecuteRequest implements Serializable {

    @Schema(description = "Agent ID", required = true)
    private String agentId;

    @Schema(description = "用户输入", required = true)
    private String input;

    @Schema(description = "会话ID（可选，用于多轮对话）")
    private String sessionId;

    @Schema(description = "是否启用流式输出")
    private Boolean stream = false;

    @Schema(description = "自定义参数（JSON格式）")
    private String parameters;
}
