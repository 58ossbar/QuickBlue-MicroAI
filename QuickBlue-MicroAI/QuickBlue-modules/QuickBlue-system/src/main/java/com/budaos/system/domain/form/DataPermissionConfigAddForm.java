package com.budaos.system.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 数据权限配置添加表单
 *
 * @author budaos
 */
@Data
public class DataPermissionConfigAddForm {

    @Schema(description = "配置编码")
    @NotBlank(message = "配置编码不能为空")
    @Length(max = 100, message = "配置编码最多100个字符")
    private String configCode;

    @Schema(description = "配置名称")
    @NotBlank(message = "配置名称不能为空")
    @Length(max = 100, message = "配置名称最多100个字符")
    private String configName;

    @Schema(description = "配置描述")
    @Length(max = 500, message = "配置描述最多500个字符")
    private String configDesc;

    @Schema(description = "业务模块名称")
    @Length(max = 100, message = "业务模块名称最多100个字符")
    private String businessModule;

    @Schema(description = "默认视图类型：0-仅本人 1-本部门 2-本部门及子部门 3-全部数据")
    private Integer defaultViewType;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "状态")
    private Boolean status;
}
