package com.budaos.support.domain.form;

import com.budaos.common.core.domain.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 帮助文档查询表单
 *
 * @author budaos
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "帮助文档查询表单")
public class HelpCenterQueryForm extends PageQuery {

    @Schema(description = "标题关键字")
    private String title;

    @Schema(description = "分类ID")
    private Long helpDocCatalogId;

    @Schema(description = "作者")
    private String author;
}
