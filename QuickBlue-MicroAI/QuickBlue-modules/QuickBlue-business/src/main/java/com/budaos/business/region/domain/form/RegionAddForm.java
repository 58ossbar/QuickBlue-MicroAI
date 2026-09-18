package com.budaos.business.region.domain.form;

import com.budaos.common.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 区域表（树形结构） 新建表单
 *
 * @Author zhujw
 * @Date 2025-12-20 14:34:26
 * @Copyright v1.0
 */

@Data
public class RegionAddForm extends BaseEntity {

    @Schema(description = "区域编码（唯一）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "区域编码不能为空")
    private String regionCode;

    @Schema(description = "区域名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "区域名称不能为空")
    private String regionName;

    @Schema(description = "区域简称")
    private String regionShortName;

    @Schema(description = "父级区域ID")
    private String parentId;

    @Schema(description = "区域层级：1-大区 2-省/市 3-城市 4-区县")
    private Integer level;

    @Schema(description = "排序（同层级内）")
    private Integer sortOrder = 0;

    @Schema(description = "状态：1-启用 2-禁用")
    private Integer status = 1;

    @Schema(description = "区域负责人")
    private String regionManagerName;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "备注")
    private String remark;
}
