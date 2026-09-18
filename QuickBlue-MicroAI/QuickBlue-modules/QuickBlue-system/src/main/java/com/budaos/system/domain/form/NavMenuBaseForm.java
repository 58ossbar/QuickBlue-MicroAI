package com.budaos.system.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 菜单基础表单
 *
 * @author budaos
 */
@Data
@Schema(description = "菜单基础表单")
public class NavMenuBaseForm {

    @Schema(description = "菜单名称", required = true)
    @NotBlank(message = "菜单名称不能为空")
    @Length(max = 30, message = "菜单名称最多30个字符")
    private String menuName;

    @Schema(description = "菜单类型：1-目录 2-菜单 3-功能点")
    private Integer menuType;

    @Schema(description = "父菜单ID，无上级可传0", required = true)
    @NotNull(message = "父菜单ID不能为空")
    private Long parentId;

    @Schema(description = "显示顺序")
    private Integer sort;

    @Schema(description = "路由地址")
    private String path;

    @Schema(description = "组件路径")
    private String component;

    @Schema(description = "是否为外链：0-否 1-是", required = true)
    @NotNull(message = "是否为外链不能为空")
    private Boolean frameFlag;

    @Schema(description = "外链地址")
    private String frameUrl;

    @Schema(description = "是否缓存：0-否 1-是", required = true)
    @NotNull(message = "是否缓存不能为空")
    private Boolean cacheFlag;

    @Schema(description = "显示状态：0-隐藏 1-显示", required = true)
    @NotNull(message = "显示状态不能为空")
    private Boolean visibleFlag;

    @Schema(description = "禁用状态：0-正常 1-禁用", required = true)
    @NotNull(message = "禁用状态不能为空")
    private Boolean disabledFlag;

    @Schema(description = "权限类型")
    private Integer permsType;

    @Schema(description = "前端权限字符串")
    private String webPerms;

    @Schema(description = "后端权限字符串")
    private String apiPerms;

    @Schema(description = "菜单图标")
    private String icon;

    @Schema(description = "功能点关联菜单ID")
    private Long contextMenuId;
}
