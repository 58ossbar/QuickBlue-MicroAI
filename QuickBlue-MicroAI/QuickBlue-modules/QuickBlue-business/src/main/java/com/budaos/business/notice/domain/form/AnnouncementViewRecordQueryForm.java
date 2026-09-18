package com.budaos.business.notice.domain.form;

import com.budaos.common.core.domain.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 通知公告 阅读记录查询
 *

 */
@Data
public class AnnouncementViewRecordQueryForm extends PageQuery {

    @Schema(description = "通知公告id")
    @NotNull(message = "通知公告id不能为空")
    private Long noticeId;

    @Schema(description = "部门id")
    private Long departmentId;

    @Schema(description = "关键字")
    private String keywords;


}
