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
 * AI模型配置实体
 */
@Data
@TableName("airag_model")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "AiRag模型配置")
public class AiragModel implements Serializable {
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

    /** 名称 */
    @Schema(description = "名称")
    private String name;

    /** 供应者 */
    @Schema(description = "供应者")
    private String provider;

    /** 模型类型 */
    @Schema(description = "模型类型")
    private String modelType;

    /** 模型名称 */
    @Schema(description = "模型名称")
    private String modelName;

    /** API域名 */
    @Schema(description = "API域名")
    private String baseUrl;

    /** 凭证信息 */
    @Schema(description = "凭证信息")
    private String credential;

    /** 模型参数 */
    @Schema(description = "模型参数")
    private String modelParams;

    /** 是否激活(0=未激活,1=已激活) */
    @Schema(description = "是否激活")
    private Integer activateFlag;
}
