package com.budaos.support.nacos.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 配置审计VO
 *
 * @author budaos
 * @since 2026-02-24
 */
@Data
@Schema(description = "配置审计VO")
public class ConfigAuditVO {

    @Schema(description = "审计ID")
    private Long auditId;

    @Schema(description = "配置ID")
    private String dataId;

    @Schema(description = "配置分组")
    private String groupId;

    @Schema(description = "命名空间ID")
    private String tenantId;

    @Schema(description = "配置名称")
    private String configName;

    @Schema(description = "修改前内容")
    private String oldContent;

    @Schema(description = "修改后内容")
    private String newContent;

    @Schema(description = "操作类型")
    private String opType;

    @Schema(description = "操作类型描述")
    private String opTypeDesc;

    @Schema(description = "操作人ID")
    private Long operatorId;

    @Schema(description = "操作人名称")
    private String operatorName;

    @Schema(description = "操作IP")
    private String operatorIp;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
