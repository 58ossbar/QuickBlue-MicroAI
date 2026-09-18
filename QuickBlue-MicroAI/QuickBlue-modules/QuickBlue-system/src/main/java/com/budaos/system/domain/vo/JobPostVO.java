package com.budaos.system.domain.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 职务表列表VO
 *
 * @author budaos
 */
@Data
public class JobPostVO {

    @Schema(description = "职务ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long positionId;

    @Schema(description = "职务名称")
    private String positionName;

    @Schema(description = "岗位编码")
    private String positionCode;

    @Schema(description = "岗位类别编码")
    private String category;

    @Schema(description = "岗位类别名称")
    private String categoryName;

    @Schema(description = "职级")
    private String positionLevel;

    @Schema(description = "职级等级")
    private Integer gradeLevel;

    @Schema(description = "父级岗位ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long parentId;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态（1启用 0停用）")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "岗位职责描述")
    private String description;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

}
