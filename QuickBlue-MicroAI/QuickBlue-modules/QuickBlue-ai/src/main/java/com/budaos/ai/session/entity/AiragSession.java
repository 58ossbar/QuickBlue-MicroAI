package com.budaos.ai.session.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * AI会话实体
 */
@Data
@TableName("airag_session")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "AI会话")
public class AiragSession implements Serializable {
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

    /** 应用ID */
    @Schema(description = "应用ID")
    private String appId;

    /** 用户ID */
    @Schema(description = "用户ID")
    private String userId;

    /** 会话标题 */
    @Schema(description = "会话标题")
    private String title;

    /** 会话摘要 */
    @Schema(description = "会话摘要")
    private String summary;

    /** 消息数量 */
    @Schema(description = "消息数量")
    private Integer msgCount;

    /** 总Token数 */
    @Schema(description = "总Token数")
    private Integer totalTokens;

    /** 最后一条消息 */
    @Schema(description = "最后一条消息")
    private String lastMessage;

    /** 最后活跃时间 */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "最后活跃时间")
    private Date lastActiveTime;

    /** 状态(0=正常,1=已删除,2=已归档) */
    @Schema(description = "状态")
    private Integer status;

    /** 扩展元数据 */
    @Schema(description = "扩展元数据")
    private String metadata;

    /** 逻辑删除 */
    @TableLogic
    @Schema(description = "逻辑删除")
    private Integer delFlag;
}
