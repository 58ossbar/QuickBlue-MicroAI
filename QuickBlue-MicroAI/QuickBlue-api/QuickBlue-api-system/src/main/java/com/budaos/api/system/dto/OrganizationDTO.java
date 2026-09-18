package com.budaos.api.system.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 部门DTO
 *
 * @author budaos
 */
@Data
public class OrganizationDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 部门ID
     */
    private Long departmentId;

    /**
     * 部门名称
     */
    private String departmentName;

    /**
     * 部门编码
     */
    private String departmentCode;

    /**
     * 上级部门ID
     */
    private Long parentId;

    /**
     * 部门级别
     */
    private Integer level;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 备注
     */
    private String remark;

    /**
     * 状态（1-启用 2-禁用）
     */
    private Integer status;

    /**
     * 子部门列表
     */
    private List<OrganizationDTO> children;
}
