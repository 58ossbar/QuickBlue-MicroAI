package com.budaos.support.nacos.domain.form;

import com.budaos.common.core.domain.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 配置审计查询表单
 *
 * @author budaos
 * @since 2026-02-24
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "配置审计查询表单")
public class ConfigAuditQueryForm extends PageQuery {

    @Schema(description = "配置ID")
    private String dataId;

    @Schema(description = "配置分组")
    private String groupId;

    @Schema(description = "命名空间ID")
    private String tenantId;

    @Schema(description = "操作类型: CREATE/UPDATE/DELETE")
    private String opType;

    @Schema(description = "操作人名称")
    private String operatorName;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;
}
