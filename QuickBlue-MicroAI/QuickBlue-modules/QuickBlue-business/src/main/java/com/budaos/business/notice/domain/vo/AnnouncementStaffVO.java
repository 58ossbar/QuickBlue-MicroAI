package com.budaos.business.notice.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * 通知公告 员工查看
 *
 * @author budaos
 * @date 2022-08-12 21:40:39
 */
@Data
public class AnnouncementStaffVO extends AnnouncementVO {

    @Schema(description = "是否查看")
    private Boolean viewFlag;

    @Schema(description = "发布日期")
    private LocalDate publishDate;

}
