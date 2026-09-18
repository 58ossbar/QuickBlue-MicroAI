package com.budaos.support.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 帮助文档完整数据VO（包含所有目录和文档内容，用于导出PDF）
 *
 * @author budaos
 */
@Data
public class HelpCenterCompleteVO {

    @Schema(description = "文档标题")
    private String title;

    @Schema(description = "目录树列表")
    private List<HelpDocCatalogTreeVO> catalogTree;

    /**
     * 目录树节点
     */
    @Data
    public static class HelpDocCatalogTreeVO {
        @Schema(description = "目录ID")
        private Long helpDocCatalogId;

        @Schema(description = "目录名称")
        private String name;

        @Schema(description = "排序")
        private Integer sort;

        @Schema(description = "父级目录ID")
        private Long parentId;

        @Schema(description = "该目录下的文档列表")
        private List<HelpCenterDetailVO> docList;

        @Schema(description = "子目录列表")
        private List<HelpDocCatalogTreeVO> children;
    }
}
