package com.budaos.support.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 帮助文档目录
 *
 * @author budaos
 */
@Data
public class HelpCenterCatalogAddForm {

    @Schema(description = "名称")
    @NotBlank(message = "名称不能为空")
    @Size(max = 200, message = "名称最多200字符")
    private String name;

    @Schema(description = "父级")
    private Long parentId;

    @Schema(description = "排序")
    private Integer sort;
}
