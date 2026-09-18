package com.budaos.support.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 配置信息
 */
@Data
public class SystemConfigVO {
    @Schema(description = "主键")
    private Long configId;

    @Schema(description = "参数key")
    private String configKey;

    @Schema(description = "参数的值")
    private String configValue;

    @Schema(description = "参数名称")
    private String configName;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "上次修改时间")
    private LocalDateTime updateTime;
}
