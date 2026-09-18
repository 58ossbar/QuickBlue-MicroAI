package com.budaos.system.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 部门树形VO
 *
 * @author budaos
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "部门树形VO")
public class OrganizationTreeVO extends OrganizationVO {

    @Schema(description = "同级上一个元素ID")
    private Long preId;

    @Schema(description = "同级下一个元素ID")
    private Long nextId;

    @Schema(description = "子部门")
    private List<OrganizationTreeVO> children;

    @Schema(description = "自己和所有递归子部门的ID集合")
    private List<Long> selfAndAllChildrenIdList;
}
