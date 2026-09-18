package com.budaos.ai.prompts.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;

/**
 * AI提示词实体
 */
@Data
@TableName("airag_prompts")
@Schema(description = "AI提示词")
public class AiragPrompts implements Serializable {
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
    private java.util.Date createTime;

    /** 更新人 */
    @Schema(description = "更新人")
    private String updateBy;

    /** 更新日期 */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新日期")
    private java.util.Date updateTime;

    /** 所属部门 */
    @Schema(description = "所属部门")
    private String sysOrgCode;

    /** 租户id */
    @Schema(description = "租户id")
    private String tenantId;

    /** 提示词名称 */
    @Schema(description = "提示词名称")
    private String name;

    /** 提示词内容 */
    @Schema(description = "提示词内容")
    private String content;

    /** 提示词类型 */
    @Schema(description = "提示词类型")
    private String type;

    /** 描述 */
    @Schema(description = "描述")
    private String description;

    /** 状态: 0-禁用 1-启用 */
    @Schema(description = "状态")
    private Integer status;
}
