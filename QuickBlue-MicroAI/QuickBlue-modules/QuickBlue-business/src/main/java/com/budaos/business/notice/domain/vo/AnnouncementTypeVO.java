package com.budaos.business.notice.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 通知公告 类型
 *
 * @author budaos
 * @date 2022-08-12 21:40:39
 */
@Data
public class AnnouncementTypeVO {

    @Schema(description = "通知类型id")
    private Long noticeTypeId;

    @Schema(description = "通知类型-名称")
    private String noticeTypeName;

}
