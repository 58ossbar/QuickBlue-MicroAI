package com.budaos.system.domain.vo;

import com.budaos.system.domain.form.NavMenuBaseForm;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 菜单VO
 *
 * @author budaos
 */
@Data
@Schema(description = "菜单VO")
public class NavMenuVO extends NavMenuBaseForm implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "菜单ID")
    private Long menuId;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "创建人")
    private Long createUserId;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "更新人")
    private Long updateUserId;

    @Schema(description = "子菜单列表")
    private List<NavMenuVO> children;
}
