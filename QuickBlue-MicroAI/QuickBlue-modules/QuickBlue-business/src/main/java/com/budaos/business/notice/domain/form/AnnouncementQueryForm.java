package com.budaos.business.notice.domain.form;

import com.budaos.common.core.domain.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * 通知公告 管理查询表单
 *

 */
@Data
public class AnnouncementQueryForm extends PageQuery {

    @Schema(description = "分类")
    private Long noticeTypeId;

    @Schema(description = "标题、作者、来源")
    private String keywords;

    @Schema(description = "文号")
    private String documentNumber;

    @Schema(description = "创建人")
    private String createUserName;

    @Schema(description = "删除标识")
    private Boolean deletedFlag;

    @Schema(description = "创建-开始时间")
    private LocalDate createTimeBegin;

    @Schema(description = "创建-截止时间")
    private LocalDate createTimeEnd;

    @Schema(description = "发布-开始时间")
    private LocalDate publishTimeBegin;

    @Schema(description = "发布-截止时间")
    private LocalDate publishTimeEnd;

}
