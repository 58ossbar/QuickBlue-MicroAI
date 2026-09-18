package com.budaos.system.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 简易菜单VO
 *
 * @author budaos
 */
@Data
public class NavMenuSimpleTreeVO {

    @Schema(description = "菜单ID")
    private Long menuId;

    @Schema(description = "菜单名称")
    private String menuName;

    @Schema(description = "功能点关联菜单ID")
    private Long contextMenuId;

    @Schema(description = "父级菜单ID")
    private Long parentId;

    @Schema(description = "菜单类型")
    private Integer menuType;

    @Schema(description = "子菜单")
    private List<NavMenuSimpleTreeVO> children;

}
