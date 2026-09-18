package com.budaos.support.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 帮助文档目录
 *
 * @author budaos
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class HelpCenterCatalogUpdateForm extends HelpCenterCatalogAddForm {

    @Schema(description = "id")
    @NotNull(message = "id")
    private Long helpDocCatalogId;
}
