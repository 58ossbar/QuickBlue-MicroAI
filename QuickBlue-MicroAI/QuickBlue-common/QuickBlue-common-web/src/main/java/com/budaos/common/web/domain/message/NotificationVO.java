package com.budaos.common.web.domain.message;

import com.budaos.common.core.domain.BaseVO;
import com.budaos.common.web.enumeration.NotificationTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 消息VO
 *
 * @author budaos
 * @since 2026-02-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "消息VO")
public class NotificationVO extends BaseVO {

    @Schema(description = "消息ID")
    private Long messageId;

    @Schema(description = "消息类型")
    private Integer messageType;

    @Schema(description = "接收者类型")
    private Integer receiverUserType;

    @Schema(description = "接收者ID")
    private Long receiverUserId;

    @Schema(description = "相关业务ID")
    private String dataId;

    @Schema(description = "消息标题")
    private String title;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "是否已读")
    private Boolean readFlag;

    @Schema(description = "已读时间")
    private LocalDateTime readTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
