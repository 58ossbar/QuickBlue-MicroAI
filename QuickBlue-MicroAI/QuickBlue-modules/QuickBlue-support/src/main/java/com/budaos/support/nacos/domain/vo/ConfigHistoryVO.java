package com.budaos.support.nacos.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 配置历史版本VO
 *
 * @author budaos
 * @since 2026-02-24
 */
@Data
@Schema(description = "配置历史版本VO")
public class ConfigHistoryVO {

    @Schema(description = "历史版本ID")
    private String id;

    @Schema(description = "配置ID")
    private String dataId;

    @Schema(description = "配置分组")
    private String groupId;

    @Schema(description = "命名空间ID")
    private String tenantId;

    @Schema(description = "应用名称")
    private String appName;

    @Schema(description = "MD5值")
    private String md5;

    @Schema(description = "配置内容")
    private String content;

    @Schema(description = "操作来源IP")
    private String srcIp;

    @Schema(description = "操作用户")
    private String srcUser;

    @Schema(description = "操作类型")
    private String opType;

    @Schema(description = "创建时间")
    private String createdTime;

    @Schema(description = "最后修改时间")
    private String lastModifiedTime;
}
