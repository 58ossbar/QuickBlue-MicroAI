package com.budaos.api.business.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知公告DTO
 *
 * @author budaos
 */
@Data
public class AnnouncementDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 公告ID
     */
    private Long noticeId;

    /**
     * 公告标题
     */
    private String title;

    /**
     * 公告内容
     */
    private String content;

    /**
     * 公告类型ID
     */
    private Long noticeTypeId;

    /**
     * 公告类型名称
     */
    private String noticeTypeName;

    /**
     * 发布人ID
     */
    private Long publisherId;

    /**
     * 发布人姓名
     */
    private String publisherName;

    /**
     * 发布时间
     */
    private LocalDateTime publishTime;

    /**
     * 状态（1-草稿 2-已发布 3-已撤回）
     */
    private Integer status;

    /**
     * 优先级（1-普通 2-重要 3-紧急）
     */
    private Integer priority;

    /**
     * 浏览次数
     */
    private Long viewCount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
