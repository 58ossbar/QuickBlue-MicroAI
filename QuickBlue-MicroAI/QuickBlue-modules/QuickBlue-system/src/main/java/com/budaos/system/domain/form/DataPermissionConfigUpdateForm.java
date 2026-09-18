package com.budaos.system.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据权限配置更新表单
 *
 * @author budaos
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DataPermissionConfigUpdateForm extends DataPermissionConfigAddForm {

    @Schema(description = "配置ID")
    @NotNull(message = "配置ID不能为空")
    private Long configId;
}
