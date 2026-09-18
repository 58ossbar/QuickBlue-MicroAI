package com.budaos.ai.llm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;

/**
 * AI知识库实体
 */
@Schema(description = "AIRag知识库")
@Data
@TableName("airag_knowledge")
public class AiragKnowledge implements Serializable {
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

    /** 知识库名称 */
    @NotBlank(message = "知识库名称不能为空")
    @Length(max = 100, message = "知识库名称不能超过100个字符")
    @Schema(description = "知识库名称")
    private String name;

    /** 向量模型id */
    @NotBlank(message = "向量模型不能为空")
    @Schema(description = "向量模型id")
    private String embedId;

    /** 描述 */
    @Length(max = 500, message = "知识库描述不能超过500个字符")
    @Schema(description = "描述")
    private String descr;

    /** 状态 */
    @Schema(description = "状态")
    private String status;

    /** 类型(knowledge知识 memory 记忆) */
    @Schema(description = "类型(knowledge知识 memory 记忆)")
    private String type;
}
