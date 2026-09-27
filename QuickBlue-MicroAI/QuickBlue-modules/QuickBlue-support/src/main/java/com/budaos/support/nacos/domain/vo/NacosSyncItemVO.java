package com.budaos.support.nacos.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Nacos配置同步明细VO
 *
 * @author budaos
 * @since 2026-09-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Nacos配置同步明细VO")
public class NacosSyncItemVO {

    @Schema(description = "文件名")
    private String fileName;

    @Schema(description = "配置ID")
    private String dataId;

    @Schema(description = "是否成功")
    private Boolean success;

    @Schema(description = "结果描述")
    private String message;
}
