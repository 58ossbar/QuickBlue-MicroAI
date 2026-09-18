package com.budaos.business.notice.domain.form;

import com.budaos.common.core.domain.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * 通知公告 员工查询表单
 *

 */
@Data
public class AnnouncementStaffQueryForm extends PageQuery {

    @Schema(description = "标题、作者、来源、文号")
    private String keywords;

    @Schema(description = "分类")
    private Long noticeTypeId;

    @Schema(description = "发布-开始时间")
    private LocalDate publishTimeBegin;

    @Schema(description = "未读标识")
    private Boolean notViewFlag;

    @Schema(description = "发布-截止时间")
    private LocalDate publishTimeEnd;
}
