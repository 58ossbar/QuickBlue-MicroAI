package com.budaos.system.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 角色的数据范围VO
 *
 * @author budaos
 */
@Data
public class AuthRoleDataPermissionVO {

    @Schema(description = "数据范围ID")
    private Integer dataScopeType;

    @Schema(description = "可见范围")
    private Integer viewType;
}
