package com.budaos.ai.llm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * AI知识库文档实体
 */
@Schema(description = "airag知识库文档")
@Data
@TableName("airag_knowledge_doc")
public class AiragKnowledgeDoc implements Serializable {
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

    /** 知识库id */
    @Schema(description = "知识库id")
    private String knowledgeId;

    /** 标题 */
    @Schema(description = "标题")
    private String title;

    /** 类型 */
    @Schema(description = "类型")
    private String type;

    /** 内容 */
    @Schema(description = "内容")
    private String content;

    /** 元数据 */
    @Schema(description = "元数据")
    private String metadata;

    /** 状态 */
    @Schema(description = "状态")
    private String status;
}
