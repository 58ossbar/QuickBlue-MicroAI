package com.budaos.system.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 数据范围和视图类型VO
 *
 * @author budaos
 */
@Data
public class DataPermissionAndViewTypeVO {

    @Schema(description = "数据范围类型")
    private Integer dataScopeType;

    @Schema(description = "数据范围名称")
    private String dataScopeTypeName;

    @Schema(description = "描述")
    private String dataScopeTypeDesc;

    @Schema(description = "顺序")
    private Integer dataScopeTypeSort;

    @Schema(description = "默认视图类型")
    private Integer defaultViewType;

    @Schema(description = "可见范围列表")
    private List<DataPermissionViewTypeVO> viewTypeList;
}
