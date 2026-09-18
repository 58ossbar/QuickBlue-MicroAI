package com.budaos.ai.llm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * MCP插件实体
 */
@Data
@TableName("airag_mcp")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "MCP插件配置")
public class AiragMcp implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
    private String id;

    /** 创建人 */
    @Schema(description = "创建人")
    private String createBy;

    /** 创建日期 */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建日期")
    private Date createTime;

    /** 更新人 */
    @Schema(description = "更新人")
    private String updateBy;

    /** 更新日期 */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新日期")
    private Date updateTime;

    /** 所属部门 */
    @Schema(description = "所属部门")
    private String sysOrgCode;

    /** 租户id */
    @Schema(description = "租户id")
    private String tenantId;

    /** 插件名称 */
    @Schema(description = "插件名称")
    private String name;

    /** 插件描述 */
    @Schema(description = "插件描述")
    private String description;

    /** 插件类型: stdio, sse, http */
    @Schema(description = "插件类型")
    private String type;

    /** 命令/URL */
    @Schema(description = "命令/URL")
    private String command;

    /** 参数 */
    @Schema(description = "参数")
    private String args;

    /** 环境变量 */
    @Schema(description = "环境变量")
    private String env;

    /** 状态: 0-禁用 1-启用 */
    @Schema(description = "状态")
    private Integer status;
}
