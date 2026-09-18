package com.budaos.ai.session.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * AI消息实体
 */
@Data
@TableName("airag_message")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "AI消息")
public class AiragMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
    private String id;

    /** 创建日期 */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建日期")
    private Date createTime;

    /** 租户id */
    @Schema(description = "租户id")
    private String tenantId;

    /** 会话ID */
    @Schema(description = "会话ID")
    private String sessionId;

    /** 角色(system/user/assistant/tool) */
    @Schema(description = "角色")
    private String role;

    /** 消息内容 */
    @Schema(description = "消息内容")
    private String content;

    /** 推理内容(DeepSeek等支持) */
    @Schema(description = "推理内容")
    private String reasoningContent;

    /** 工具调用JSON */
    @Schema(description = "工具调用")
    private String toolCalls;

    /** 工具调用ID */
    @Schema(description = "工具调用ID")
    private String toolCallId;

    /** 工具名称 */
    @Schema(description = "工具名称")
    private String toolName;

    /** 输入Token数 */
    @Schema(description = "输入Token数")
    private Integer inputTokens;

    /** 输出Token数 */
    @Schema(description = "输出Token数")
    private Integer outputTokens;

    /** 总Token数 */
    @Schema(description = "总Token数")
    private Integer totalTokens;

    /** 模型名称 */
    @Schema(description = "模型名称")
    private String modelName;

    /** 响应时间(毫秒) */
    @Schema(description = "响应时间")
    private Long responseTime;

    /** 消息状态(0=成功,1=失败,2=中断) */
    @Schema(description = "消息状态")
    private Integer status;

    /** 错误信息 */
    @Schema(description = "错误信息")
    private String errorMsg;

    /** 扩展元数据 */
    @Schema(description = "扩展元数据")
    private String metadata;

    /**
     * 消息角色枚举
     */
    public enum Role {
        SYSTEM("system"),
        USER("user"),
        ASSISTANT("assistant"),
        TOOL("tool");

        private final String value;

        Role(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
