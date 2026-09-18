package com.budaos.system.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 员工信息VO
 *
 * @author budaos
 */
@Data
@Schema(description = "员工信息VO")
public class StaffVO {

    @Schema(description = "员工ID")
    private Long employeeId;

    @Schema(description = "员工UID")
    private String employeeUid;

    @Schema(description = "登录账号")
    private String loginName;

    @Schema(description = "真实姓名")
    private String actualName;

    @Schema(description = "性别 1-男 2-女")
    private Integer gender;

    @Schema(description = "手机号码")
    private String phone;

    @Schema(description = "部门ID")
    private Long departmentId;

    @Schema(description = "部门名称")
    private String departmentName;

    @Schema(description = "是否被禁用")
    private Boolean disabledFlag;

    @Schema(description = "是否超级管理员")
    private Boolean administratorFlag;

    @Schema(description = "是否已删除")
    private Boolean deletedFlag;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "角色ID列表")
    private List<Long> roleIdList;

    @Schema(description = "角色名称列表")
    private List<String> roleNameList;

    @Schema(description = "岗位ID")
    private Long positionId;

    @Schema(description = "岗位名称")
    private String positionName;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "备注")
    private String remark;
}
