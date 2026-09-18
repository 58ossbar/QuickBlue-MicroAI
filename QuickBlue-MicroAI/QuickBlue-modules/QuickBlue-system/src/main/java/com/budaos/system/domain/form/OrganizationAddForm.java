package com.budaos.system.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 部门添加表单
 *
 * @author budaos
 */
@Data
@Schema(description = "部门添加表单")
public class OrganizationAddForm {

    @Schema(description = "部门名称", required = true)
    @Length(min = 1, max = 50, message = "请输入正确的部门名称(1-50个字符)")
    @NotNull(message = "请输入正确的部门名称(1-50个字符)")
    private String departmentName;

    @Schema(description = "排序", required = true)
    @NotNull(message = "排序值不能为空")
    private Integer sort;

    @Schema(description = "部门负责人ID")
    private Long managerId;

    @Schema(description = "上级部门ID（可选）")
    private Long parentId;
}
