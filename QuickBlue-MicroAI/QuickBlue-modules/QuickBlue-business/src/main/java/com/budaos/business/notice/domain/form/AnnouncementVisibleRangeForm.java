package com.budaos.business.notice.domain.form;

import com.budaos.business.notice.constant.AnnouncementVisibleRangeDataTypeEnum;
import com.budaos.common.core.annotation.EnumCheck;
import com.budaos.common.core.annotation.ApiSchemaEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通知公告 可见范围数据

 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementVisibleRangeForm {

    @ApiSchemaEnum(AnnouncementVisibleRangeDataTypeEnum.class)
    @EnumCheck(value = AnnouncementVisibleRangeDataTypeEnum.class, required = true, message = "数据类型错误")
    private Integer dataType;

    @Schema(description = "员工/部门id")
    @NotNull(message = "员工/部门id不能为空")
    private Long dataId;
}
