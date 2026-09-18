package com.budaos.support.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 系统更新日志 VO
 *
 * @author budaos
 */
@Data
@Schema(description = "系统更新日志VO")
public class ReleaseLogVO {

    @Schema(description = "更新日志ID")
    private Long changeLogId;

    @Schema(description = "版本")
    private String updateVersion;

    @Schema(description = "更新类型:[1:特大版本功能更新;2:功能更新;3:bug修复]")
    private Integer type;

    @Schema(description = "更新类型名称")
    private String typeName;

    @Schema(description = "发布人")
    private String publishAuthor;

    @Schema(description = "发布日期")
    private LocalDate publicDate;

    @Schema(description = "更新内容")
    private String content;

    @Schema(description = "跳转链接")
    private String link;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
