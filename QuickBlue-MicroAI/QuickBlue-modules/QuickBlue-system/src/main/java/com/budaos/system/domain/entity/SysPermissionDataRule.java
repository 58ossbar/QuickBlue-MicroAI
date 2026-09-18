package com.budaos.system.domain.entity;

import com.budaos.common.core.domain.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据权限规则实体
 *
 * @author QuickBlue
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_permission_data_rule")
@Schema(description = "数据权限规则实体")
public class SysPermissionDataRule extends BaseEntity {

    @Schema(description = "规则ID")
    private Long ruleId;

    @Schema(description = "权限ID（菜单权限ID）")
    private Long permissionId;

    @Schema(description = "规则名称")
    private String ruleName;

    @Schema(description = "规则字段")
    private String ruleColumn;

    @Schema(description = "规则条件（=、!=、>、<、like、in等）")
    private String ruleConditions;

    @Schema(description = "规则值（支持SQL变量：#{sys_user_code}、#{sys_user_id}、#{sys_depart_id}、#{sys_depart_ids}、#{sys_user_name}）")
    private String ruleValue;

    @Schema(description = "状态（0-禁用，1-启用）")
    private Integer status;

    @Schema(description = "排序")
    private Integer sortOrder;
}
