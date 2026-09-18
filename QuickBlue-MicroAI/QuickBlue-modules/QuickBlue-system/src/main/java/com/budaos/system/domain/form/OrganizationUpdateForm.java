package com.budaos.system.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 部门更新表单
 *
 * @author budaos
 */
@Data
@Schema(description = "部门更新表单")
public class OrganizationUpdateForm extends OrganizationAddForm {

    @Schema(description = "部门ID", required = true)
    @NotNull(message = "部门ID不能为空")
    private Long departmentId;
}
