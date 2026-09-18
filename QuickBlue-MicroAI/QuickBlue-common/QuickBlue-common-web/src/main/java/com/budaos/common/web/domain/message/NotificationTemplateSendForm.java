package com.budaos.common.web.domain.message;

import com.budaos.common.web.enumeration.NotificationTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * 模板消息发送Form
 *
 * @author budaos
 * @since 2026-02-11
 */
@Data
@Schema(description = "模板消息发送Form")
public class NotificationTemplateSendForm {

    @Schema(description = "消息模板类型")
    private NotificationTypeEnum messageTemplateEnum;

    @Schema(description = "接收者类型")
    private Integer receiverUserType;

    @Schema(description = "接收者ID")
    private Long receiverUserId;

    @Schema(description = "模板内容参数")
    private Map<String, String> contentParam;

    /**
     * 相关业务ID | 可选
     */
    @Schema(description = "相关业务ID")
    private Object dataId;
}
