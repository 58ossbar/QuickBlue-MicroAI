package com.budaos.business.notice.domain.vo;

import com.budaos.business.notice.constant.AnnouncementVisibleRangeDataTypeEnum;
import com.budaos.common.core.annotation.ApiSchemaEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 新闻、公告 可见范围数据 VO
 *
 * @author budaos
 * @date 2022-08-12 21:40:39
 */
@Data
public class AnnouncementVisibleRangeVO {

    @ApiSchemaEnum(AnnouncementVisibleRangeDataTypeEnum.class)
    private Integer dataType;

    @Schema(description = "员工/部门id")
    private Long dataId;

    @Schema(description = "员工/部门 名称")
    private String dataName;

}
