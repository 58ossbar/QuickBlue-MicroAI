package com.budaos.business.notice.domain.vo;

import com.budaos.business.notice.util.AttachmentKeyVoSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 用于更新 【通知、公告】 的 VO 对象
 *
 * @author budaos
 * @date 2022-08-12 21:40:39
 */
@Data
public class AnnouncementUpdateFormVO extends AnnouncementVO {

    @Schema(description = "纯文本内容")
    private String contentText;

    @Schema(description = "html内容")
    private String contentHtml;

    @Schema(description = "附件")
    @JsonSerialize(using = AttachmentKeyVoSerializer.class)
    private String attachment;

    @Schema(description = "可见范围")
    private List<AnnouncementVisibleRangeVO> visibleRangeList;

}
