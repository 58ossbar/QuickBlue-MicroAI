package com.budaos.system.domain.form;

import com.budaos.common.core.domain.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

/**
 * 数据权限配置查询表单
 *
 * @author budaos
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DataPermissionConfigQueryForm extends PageQuery {

    @Schema(description = "配置编码")
    @Length(max = 100, message = "配置编码最多100个字符")
    private String configCode;

    @Schema(description = "配置名称")
    @Length(max = 100, message = "配置名称最多100个字符")
    private String configName;

    @Schema(description = "业务模块名称")
    @Length(max = 100, message = "业务模块名称最多100个字符")
    private String businessModule;

    @Schema(description = "状态")
    private Boolean status;
}
