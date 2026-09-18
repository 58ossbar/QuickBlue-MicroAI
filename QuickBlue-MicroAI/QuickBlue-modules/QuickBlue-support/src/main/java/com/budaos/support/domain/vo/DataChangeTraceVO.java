package com.budaos.support.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据变动记录 VO
 *
 * @author QuickBlue
 * @date 2026-02-08
 */
@Data
@Schema(description = "数据变动记录 VO")
public class DataChangeTraceVO {

    @Schema(description = "变动id")
    private Long dataTracerId;

    @Schema(description = "数据id")
    private Long dataId;

    @Schema(description = "业务类型")
    private Integer type;

    @Schema(description = "业务类型名称")
    private String typeName;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "差异：旧的数据")
    private String diffOld;

    @Schema(description = "差异：新的数据")
    private String diffNew;

    @Schema(description = "用户")
    private Long userId;

    @Schema(description = "用户类型")
    private Integer userType;

    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "请求ip")
    private String ip;

    @Schema(description = "请求ip地区")
    private String ipRegion;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
