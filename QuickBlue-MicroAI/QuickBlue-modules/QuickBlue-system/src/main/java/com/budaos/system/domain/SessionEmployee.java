package com.budaos.system.domain;

import com.budaos.system.common.enums.AccountTypeEnum;
import com.budaos.common.core.domain.CurrentUser;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 请求员工信息
 *
 * @author budaos
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "请求员工信息")
public class SessionEmployee implements CurrentUser {

    @Schema(description = "员工id")
    private Long employeeId;

    @Schema(description = "登录名")
    private String loginName;

    @Schema(description = "真实姓名")
    private String actualName;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "部门id")
    private Long departmentId;

    @Schema(description = "部门名称")
    private String departmentName;

    @Schema(description = "岗位id")
    private Long positionId;

    @Schema(description = "岗位名称")
    private String positionName;

    @Schema(description = "是否管理员")
    private Boolean administratorFlag;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "请求ip")
    private String ip;

    @Schema(description = "请求user-agent")
    private String userAgent;

    @Override
    public Long getUserId() {
        return employeeId;
    }

    @Override
    public String getUserName() {
        return actualName;
    }

    @Override
    public Integer getUserType() {
        return AccountTypeEnum.ADMIN_EMPLOYEE.getValue();
    }

    @Override
    public String getIp() {
        return ip;
    }

    @Override
    public String getUserAgent() {
        return userAgent;
    }
}
