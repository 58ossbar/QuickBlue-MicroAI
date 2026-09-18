package com.budaos.ai.agent.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Agent执行响应DTO
 */
@Data
@Schema(description = "Agent执行响应")
public class AgentExecuteResponse implements Serializable {

    @Schema(description = "Agent ID")
    private String agentId;

    @Schema(description = "会话ID")
    private String sessionId;

    @Schema(description = "Agent输出的最终回答")
    private String answer;

    @Schema(description = "执行的推理步骤")
    private List<ReasoningStep> reasoningSteps;

    @Schema(description = "使用的工具")
    private List<String> toolsUsed;

    @Schema(description = "检索到的文档片段（RAG）")
    private List<DocumentReference> retrievedDocuments;

    @Schema(description = "Token统计")
    private TokenStats tokenStats;

    @Schema(description = "执行耗时（毫秒）")
    private Long duration;

    @Schema(description = "是否成功")
    private Boolean success;

    @Schema(description = "错误信息")
    private String error;

    @Schema(description = "扩展数据")
    private Map<String, Object> metadata;

    /**
     * 推理步骤
     */
    @Data
    @Schema(description = "推理步骤")
    public static class ReasoningStep implements Serializable {
        @Schema(description = "步骤序号")
        private Integer stepNumber;

        @Schema(description = "步骤类型（thought/action/observation）")
        private String stepType;

        @Schema(description = "步骤内容")
        private String content;

        @Schema(description = "工具调用详情")
        private ToolCall toolCall;
    }

    /**
     * 工具调用
     */
    @Data
    @Schema(description = "工具调用")
    public static class ToolCall implements Serializable {
        @Schema(description = "工具名称")
        private String toolName;

        @Schema(description = "工具参数")
        private String arguments;

        @Schema(description = "工具返回结果")
        private String result;
    }

    /**
     * 文档引用
     */
    @Data
    @Schema(description = "文档引用")
    public static class DocumentReference implements Serializable {
        @Schema(description = "文档ID")
        private String documentId;

        @Schema(description = "文档标题")
        private String title;

        @Schema(description = "相关度分数")
        private Double score;

        @Schema(description = "内容片段")
        private String snippet;
    }

    /**
     * Token统计
     */
    @Data
    @Schema(description = "Token统计")
    public static class TokenStats implements Serializable {
        @Schema(description = "输入Token数")
        private Integer inputTokens;

        @Schema(description = "输出Token数")
        private Integer outputTokens;

        @Schema(description = "总Token数")
        private Integer totalTokens;
    }
}
