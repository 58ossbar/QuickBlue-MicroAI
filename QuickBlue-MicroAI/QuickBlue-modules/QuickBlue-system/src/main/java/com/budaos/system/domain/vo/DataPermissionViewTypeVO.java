package com.budaos.system.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 数据可见范围VO
 *
 * @author budaos
 */
@Data
@Builder
public class DataPermissionViewTypeVO {

    @Schema(description = "可见范围")
    private Integer viewType;

    @Schema(description = "可见范围名称")
    private String viewTypeName;

    @Schema(description = "级别，用于表示范围大小")
    private Integer viewTypeLevel;
}
