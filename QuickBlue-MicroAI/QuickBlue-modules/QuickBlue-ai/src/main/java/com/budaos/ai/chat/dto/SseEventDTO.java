package com.budaos.ai.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * SSE事件DTO
 */
@Data
@Schema(description = "SSE事件DTO")
public class SseEventDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 事件类型 */
    @Schema(description = "事件类型")
    private String event;

    /** 数据内容 */
    @Schema(description = "数据内容")
    private Object data;

    /** 消息ID */
    @Schema(description = "消息ID")
    private String id;

    /** 重试时间(毫秒) */
    @Schema(description = "重试时间")
    private Long retry;

    /**
     * 创建消息事件
     */
    public static SseEventDTO message(Object data) {
        SseEventDTO dto = new SseEventDTO();
        dto.setEvent("message");
        dto.setData(data);
        return dto;
    }

    /**
     * 创建完成事件
     */
    public static SseEventDTO done(String sessionId) {
        SseEventDTO dto = new SseEventDTO();
        dto.setEvent("done");
        dto.setData("[DONE]");
        dto.setId(sessionId);
        return dto;
    }

    /**
     * 创建错误事件
     */
    public static SseEventDTO error(String message) {
        SseEventDTO dto = new SseEventDTO();
        dto.setEvent("error");
        dto.setData(message);
        return dto;
    }

    /**
     * 创建心跳事件
     */
    public static SseEventDTO heartbeat() {
        SseEventDTO dto = new SseEventDTO();
        dto.setEvent("heartbeat");
        dto.setData("ping");
        return dto;
    }

    /**
     * 创建Token统计事件
     */
    public static SseEventDTO tokens(Integer inputTokens, Integer outputTokens, Integer totalTokens) {
        SseEventDTO dto = new SseEventDTO();
        dto.setEvent("tokens");
        TokenData data = new TokenData();
        data.setInputTokens(inputTokens);
        data.setOutputTokens(outputTokens);
        data.setTotalTokens(totalTokens);
        dto.setData(data);
        return dto;
    }

    @Data
    public static class TokenData implements Serializable {
        private Integer inputTokens;
        private Integer outputTokens;
        private Integer totalTokens;
    }
}
