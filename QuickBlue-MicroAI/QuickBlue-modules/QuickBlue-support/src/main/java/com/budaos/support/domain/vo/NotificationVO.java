package com.budaos.support.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息VO
 *
 * @author budaos
 */
@Data
public class NotificationVO {

    /**
     * 消息ID
     */
    private Long messageId;

    /**
     * 消息类型
     */
    private Integer messageType;

    /**
     * 接收者类型
     */
    private Integer receiverUserType;

    /**
     * 接收者ID
     */
    private Long receiverUserId;

    /**
     * 相关业务ID
     */
    private String dataId;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 是否已读
     */
    private Boolean readFlag;

    /**
     * 已读时间
     */
    private LocalDateTime readTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
