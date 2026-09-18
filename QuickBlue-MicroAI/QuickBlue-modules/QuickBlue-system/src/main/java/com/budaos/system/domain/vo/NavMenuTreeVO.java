package com.budaos.system.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 菜单树形VO
 *
 * @author budaos
 */
@Data
@Schema(description = "菜单树形VO")
public class NavMenuTreeVO {

    @Schema(description = "菜单ID")
    private Long menuId;

    @Schema(description = "父级菜单ID")
    private Long parentId;

    @Schema(description = "菜单名称")
    private String menuName;

    @Schema(description = "菜单类型")
    private Integer menuType;

    @Schema(description = "权限标识")
    private String perms;

    @Schema(description = "路径")
    private String path;

    @Schema(description = "组件")
    private String component;

    @Schema(description = "重定向")
    private String redirect;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "子菜单")
    private List<NavMenuTreeVO> children;
}
