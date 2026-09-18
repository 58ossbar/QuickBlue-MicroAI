package com.budaos.system.domain.form;

import com.budaos.common.core.domain.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 职务查询表单
 *
 * @author budaos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class JobPostQueryForm extends PageQuery {

    @Schema(description = "职务名称（模糊查询）")
    private String positionName;

    @Schema(description = "岗位类别")
    private String category;

    @Schema(description = "状态（1启用 0停用）")
    private Integer status;

    @Schema(description = "职级")
    private String positionLevel;

    @Schema(description = "删除标识", hidden = true)
    private Boolean deletedFlag;

}
