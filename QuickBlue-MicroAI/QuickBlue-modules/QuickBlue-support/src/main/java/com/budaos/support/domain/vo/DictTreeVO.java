package com.budaos.support.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 树形字典VO
 *
 * @author QuickBlue
 */
@Data
@Schema(description = "树形字典VO")
public class DictTreeVO {

    @Schema(description = "字典值")
    private String dictValue;

    @Schema(description = "字典标签")
    private String dictLabel;

    @Schema(description = "字典文本")
    private String dictText;

    @Schema(description = "父级编码")
    private String parentCode;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "子节点")
    private List<DictTreeVO> children;
}
