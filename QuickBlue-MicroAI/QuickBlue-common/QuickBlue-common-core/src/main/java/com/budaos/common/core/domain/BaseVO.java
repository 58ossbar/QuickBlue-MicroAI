package com.budaos.common.core.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * VO基类
 *
 * @author budaos
 * @since 2026-02-09
 */
@Data
public abstract class BaseVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "创建人ID")
    private Long createUserId;

    @Schema(description = "创建人姓名")
    private String createUserName;

    @Schema(description = "更新人ID")
    private Long updateUserId;

    @Schema(description = "更新人姓名")
    private String updateUserName;
}
