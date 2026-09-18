package com.budaos.common.web.domain.message;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 消息发送Form
 *
 * @author budaos
 * @since 2026-02-11
 */
@Data
@Schema(description = "消息发送Form")
public class NotificationSendForm {

    @Schema(description = "消息类型")
    @NotNull(message = "消息类型不能为空")
    private Integer messageType;

    @Schema(description = "接收者类型")
    @NotNull(message = "接收者类型不能为空")
    private Integer receiverUserType;

    @Schema(description = "接收者ID")
    @NotNull(message = "接收者ID不能为空")
    private Long receiverUserId;

    @Schema(description = "标题")
    @NotBlank(message = "标题不能为空")
    private String title;

    @Schema(description = "内容")
    @NotBlank(message = "内容不能为空")
    private String content;

    /**
     * 相关业务ID | 可选
     */
    @Schema(description = "相关业务ID")
    private Object dataId;
}
