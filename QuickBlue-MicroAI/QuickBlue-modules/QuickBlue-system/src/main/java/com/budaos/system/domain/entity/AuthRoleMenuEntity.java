package com.budaos.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色菜单关联实体
 *
 * @author budaos
 */
@Data
@TableName("t_role_menu")
public class AuthRoleMenuEntity {

    @TableId(type = IdType.AUTO)
    private Long roleMenuId;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 菜单ID
     */
    private Long menuId;

    private LocalDateTime createTime;

}
