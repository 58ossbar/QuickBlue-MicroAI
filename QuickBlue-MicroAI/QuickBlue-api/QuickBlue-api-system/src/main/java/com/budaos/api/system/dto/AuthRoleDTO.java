package com.budaos.api.system.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 角色DTO
 *
 * @author budaos
 */
@Data
public class AuthRoleDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 备注
     */
    private String remark;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态（1-启用 2-禁用）
     */
    private Integer status;

    /**
     * 菜单ID列表
     */
    private List<Long> menuIdList;
}
