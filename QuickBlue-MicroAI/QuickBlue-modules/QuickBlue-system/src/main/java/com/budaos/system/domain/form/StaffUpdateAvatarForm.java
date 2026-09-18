package com.budaos.system.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 员工更新头像表单
 *
 * @author budaos
 */
@Data
@Schema(description = "员工更新头像表单")
public class StaffUpdateAvatarForm {

    @Schema(description = "员工ID")
    private Long employeeId;

    @Schema(description = "头像URL", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "头像URL不能为空")
    @Size(max = 500, message = "头像URL最多500字符")
    private String avatar;
}
