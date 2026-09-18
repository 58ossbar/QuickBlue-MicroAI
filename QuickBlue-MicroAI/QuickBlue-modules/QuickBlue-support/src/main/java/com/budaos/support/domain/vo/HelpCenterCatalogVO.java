package com.budaos.support.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帮助文档目录 VO
 *
 * @author budaos
 */
@Data
@Schema(description = "帮助文档目录VO")
public class HelpCenterCatalogVO {

    @Schema(description = "目录ID")
    private Long helpDocCatalogId;

    @Schema(description = "目录名称")
    private String name;

    @Schema(description = "父目录ID")
    private Long parentId;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
