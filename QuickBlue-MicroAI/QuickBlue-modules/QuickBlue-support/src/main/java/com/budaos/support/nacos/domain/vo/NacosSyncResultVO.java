package com.budaos.support.nacos.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * Nacos配置同步结果VO
 *
 * @author budaos
 * @since 2026-09-27
 */
@Data
@Schema(description = "Nacos配置同步结果VO")
public class NacosSyncResultVO {

    @Schema(description = "扫描到的配置文件总数")
    private Integer totalFiles;

    @Schema(description = "同步成功数量")
    private Integer successCount;

    @Schema(description = "同步失败数量")
    private Integer failCount;

    @Schema(description = "跳过数量（Nacos中内容一致）")
    private Integer skipCount;

    @Schema(description = "同步目标命名空间")
    private String tenantId;

    @Schema(description = "同步目标分组")
    private String groupId;

    @Schema(description = "配置来源（本地目录或classpath）")
    private String source;

    @Schema(description = "同步明细")
    private List<NacosSyncItemVO> results;
}
