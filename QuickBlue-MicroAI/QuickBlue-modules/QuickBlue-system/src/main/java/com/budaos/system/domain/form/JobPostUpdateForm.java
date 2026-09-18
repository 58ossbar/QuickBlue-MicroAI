package com.budaos.system.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 职务更新表单
 *
 * @author budaos
 */
@Data
public class JobPostUpdateForm {

    @Schema(description = "职务ID")
    @NotNull(message = "职务ID不能为空")
    private Long positionId;

    @Schema(description = "职务名称")
    @NotBlank(message = "职务名称不能为空")
    private String positionName;

    @Schema(description = "岗位编码")
    private String positionCode;

    @Schema(description = "岗位类别")
    private String category;

    @Schema(description = "职级")
    private String positionLevel;

    @Schema(description = "职级等级")
    private Integer gradeLevel;

    @Schema(description = "父级岗位ID")
    private Long parentId;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态（1启用 0停用）")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "岗位职责描述")
    private String description;

}
