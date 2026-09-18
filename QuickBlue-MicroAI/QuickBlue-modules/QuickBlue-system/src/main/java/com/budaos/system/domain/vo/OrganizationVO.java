package com.budaos.system.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 部门VO
 *
 * @author budaos
 */
@Data
@Schema(description = "部门VO")
public class OrganizationVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "部门ID")
    private Long departmentId;

    @Schema(description = "部门名称")
    private String departmentName;

    @Schema(description = "部门负责人姓名")
    private String managerName;

    @Schema(description = "部门负责人ID")
    private Long managerId;

    @Schema(description = "父级部门ID")
    private Long parentId;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
