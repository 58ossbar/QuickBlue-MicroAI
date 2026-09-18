package com.budaos.business.region.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 区域表（树形结构） 分页查询表单
 *
 * @Author zhujw
 * @Date 2025-12-20 14:34:26
 * @Copyright v1.0
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class RegionQueryForm {

    @Schema(description = "区域名称")
    private String regionName;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "区域负责人姓名")
    private String regionManagerName;

    @Schema(description = "联系电话")
    private String contactPhone;

}
