package com.budaos.business.notice.domain.form;

import com.budaos.business.notice.util.AttachmentKeyVoDeserializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知公告 添加表单
 *
 */
@Data
public class AnnouncementAddForm {

    @Schema(description = "标题")
    @NotBlank(message = "标题不能为空")
    @Length(max = 200, message = "标题最多200字符")
    private String title;

    @Schema(description = "分类")
    @NotNull(message = "分类不能为空")
    private Long noticeTypeId;

    @Schema(description = "是否全部可见")
    @NotNull(message = "是否全部可见不能为空")
    private Boolean allVisibleFlag;

    @Schema(description = "是否定时发布")
    @NotNull(message = "是否定时发布不能为空")
    private Boolean scheduledPublishFlag;

    @Schema(description = "发布时间")
    @NotNull(message = "发布时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime publishTime;

    @Schema(description = "纯文本内容")
    @NotNull(message = "文本内容不能为空")
    private String contentText;

    @Schema(description = "html内容")
    @NotNull(message = "html内容不能为空")
    private String contentHtml;

    @Schema(description = "附件,多个英文逗号分隔|可选")
    @Length(max = 1000, message = "最多1000字符")
    @JsonDeserialize(using = AttachmentKeyVoDeserializer.class)
    private String attachment;

    @Schema(description = "作者")
    @NotBlank(message = "作者不能为空")
    private String author;

    @Schema(description = "来源")
    @NotBlank(message = "标题不能为空")
    private String source;

    @Schema(description = "文号")
    private String documentNumber;

    @Schema(hidden = true)
    private Long createUserId;

    @Schema(description = "可见范围设置|可选")
    @Valid
    private List<AnnouncementVisibleRangeForm> visibleRangeList;
}
