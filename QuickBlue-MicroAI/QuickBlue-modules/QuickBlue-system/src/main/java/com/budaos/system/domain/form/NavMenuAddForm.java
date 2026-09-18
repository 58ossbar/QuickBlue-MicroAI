package com.budaos.system.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 菜单添加表单
 *
 * @author budaos
 */
@Data
@Schema(description = "菜单添加表单")
public class NavMenuAddForm extends NavMenuBaseForm {

    @Schema(description = "创建人ID")
    private Long createUserId;
}
