package com.budaos.system.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 员工批量更新部门表单
 *
 * @author budaos
 */
@Data
@Schema(description = "员工批量更新部门表单")
public class StaffBatchUpdateOrganizationForm {

    @Schema(description = "员工ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "员工ID列表不能为空")
    private List<Long> employeeIdList;

    @Schema(description = "部门ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "部门ID不能为空")
    private Long departmentId;
}
