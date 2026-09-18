package com.budaos.system.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 员工个人中心更新表单
 *
 * @author budaos
 */
@Data
@Schema(description = "员工个人中心更新表单")
public class StaffUpdateCenterForm {

    @Schema(description = "员工ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long employeeId;

    @Schema(description = "真实姓名")
    @Size(max = 30, message = "姓名最多30字符")
    private String actualName;

    @Schema(description = "性别 1-男 2-女")
    @Min(value = 1, message = "性别只能为1或2")
    @Max(value = 2, message = "性别只能为1或2")
    private Integer gender;

    @Schema(description = "手机号")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Schema(description = "邮箱")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$", message = "邮箱格式不正确")
    private String email;

    @Schema(description = "头像")
    @Size(max = 500, message = "头像URL最多500字符")
    private String avatar;
}
