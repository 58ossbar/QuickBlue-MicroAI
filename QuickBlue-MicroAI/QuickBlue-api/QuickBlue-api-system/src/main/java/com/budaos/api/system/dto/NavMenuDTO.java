package com.budaos.api.system.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 菜单DTO
 *
 * @author budaos
 */
@Data
public class NavMenuDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 菜单ID
     */
    private Long menuId;

    /**
     * 菜单名称
     */
    private String menuName;

    /**
     * 菜单类型（1-目录 2-菜单 3-按钮）
     */
    private Integer menuType;

    /**
     * 父菜单ID
     */
    private Long parentId;

    /**
     * 菜单图标
     */
    private String menuIcon;

    /**
     * 请求路径
     */
    private String path;

    /**
     * 组件路径
     */
    private String component;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态（1-启用 2-禁用）
     */
    private Integer status;

    /**
     * 是否外链
     */
    private Boolean externalLinkFlag;

    /**
     * 外链地址
     */
    private String externalLinkUrl;

    /**
     * 子菜单列表
     */
    private List<NavMenuDTO> children;
}
