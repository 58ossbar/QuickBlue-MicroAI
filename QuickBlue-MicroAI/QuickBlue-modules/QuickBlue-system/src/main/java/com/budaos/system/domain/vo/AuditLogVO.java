package com.budaos.system.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志VO
 *
 * @author budaos
 */
@Data
@Schema(description = "操作日志VO")
public class AuditLogVO {

    @Schema(description = "主键ID")
    private Long operateLogId;

    @Schema(description = "用户ID")
    private Long operateUserId;

    @Schema(description = "用户类型")
    private Integer operateUserType;

    @Schema(description = "用户名称")
    private String operateUserName;

    @Schema(description = "操作模块")
    private String module;

    @Schema(description = "操作内容")
    private String content;

    @Schema(description = "请求路径")
    private String url;

    @Schema(description = "请求方法")
    private String method;

    @Schema(description = "请求参数")
    private String param;

    @Schema(description = "返回值")
    private String response;

    @Schema(description = "客户IP")
    private String ip;

    @Schema(description = "客户IP地区")
    private String ipRegion;

    @Schema(description = "User-Agent")
    private String userAgent;

    @Schema(description = "请求结果 0失败 1成功")
    private Boolean successFlag;

    @Schema(description = "失败原因")
    private String failReason;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
