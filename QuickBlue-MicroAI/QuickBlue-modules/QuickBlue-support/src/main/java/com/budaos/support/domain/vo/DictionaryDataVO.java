package com.budaos.support.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 字典数据表列表VO
 *
 * @author budaos
 */
@Data
public class DictionaryDataVO implements Serializable {

    @Schema(description = "字典数据id")
    private Long dictDataId;

    @Schema(description = "字典id")
    private Long dictId;

    @Schema(description = "字典编码")
    private String dictCode;

    @Schema(description = "字典名字")
    private String dictName;

    @Schema(description = "字典禁用状态")
    private Boolean dictDisabledFlag;

    @Schema(description = "字典项值")
    private String dataValue;

    @Schema(description = "字典项显示名称")
    private String dataLabel;

    @Schema(description = "父级编码（用于树形字典）")
    private String parentCode;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "排序（越大越靠前）")
    private Integer sortOrder;

    @Schema(description = "禁用状态")
    private Boolean disabledFlag;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

}
