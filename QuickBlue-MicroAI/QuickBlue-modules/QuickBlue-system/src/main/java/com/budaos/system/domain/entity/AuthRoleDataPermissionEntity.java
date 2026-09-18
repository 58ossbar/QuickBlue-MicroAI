package com.budaos.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据范围与角色关系实体
 *
 * @author budaos
 */
@Data
@TableName("t_role_data_scope")
public class AuthRoleDataPermissionEntity {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 数据范围类型
     * @see com.budaos.system.domain.constant.DataPermissionTypeEnum
     */
    private Integer dataScopeType;

    /**
     * 数据可见范围类型
     * @see com.budaos.system.domain.constant.DataPermissionViewTypeEnum
     */
    private Integer viewType;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
