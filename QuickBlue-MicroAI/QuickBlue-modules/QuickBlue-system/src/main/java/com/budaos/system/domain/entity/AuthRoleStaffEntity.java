package com.budaos.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色员工关联实体
 *
 * @author budaos
 */
@Data
@TableName("t_role_employee")
public class AuthRoleStaffEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long roleEmployeeId;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 员工ID
     */
    private Long employeeId;

    private LocalDateTime createTime;

}
