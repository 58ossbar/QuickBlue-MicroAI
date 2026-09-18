package com.budaos.api.support.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息DTO
 *
 * @author budaos
 */
@Data
public class NotificationDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息ID
     */
    private Long messageId;

    /**
     * 消息类型（1-系统消息 2-业务消息）
     */
    private Integer messageType;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 接收人ID
     */
    private Long receiverId;

    /**
     * 接收人姓名
     */
    private String receiverName;

    /**
     * 发送时间
     */
    private LocalDateTime sendTime;

    /**
     * 是否已读（1-未读 2-已读）
     */
    private Integer readFlag;

    /**
     * 阅读时间
     */
    private LocalDateTime readTime;
}
