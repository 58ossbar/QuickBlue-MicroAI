package com.budaos.business.region.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 区域表（树形结构） VO
 *
 * @Author zhujw
 * @Date 2025-12-20 14:34:26
 * @Copyright v1.0
 */
@Data
public class RegionVO {

    @Schema(description = "区域ID")
    private String id;

    @Schema(description = "区域编码（唯一）")
    private String regionCode;

    @Schema(description = "区域名称")
    private String regionName;

    @Schema(description = "区域简称")
    private String regionShortName;

    @Schema(description = "父级区域ID")
    private String parentId;

    @Schema(description = "父级路径（格式: /id1/id2/id3）")
    private String parentPath;

    @Schema(description = "区域层级：1-大区 2-省/市 3-城市 4-区县")
    private Integer level;

    @Schema(description = "排序（同层级内）")
    private Integer sortOrder;

    @Schema(description = "状态：1-启用 2-禁用")
    private Integer status;

    @Schema(description = "区域负责人ID")
    private Long regionManagerId;

    @Schema(description = "区域负责人姓名")
    private String regionManagerName;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "是否叶子节点：0-否 1-是")
    private Integer leafFlag;

    @Schema(description = "创建人ID")
    private Long createUserId;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新人ID")
    private Long updateUserId;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
