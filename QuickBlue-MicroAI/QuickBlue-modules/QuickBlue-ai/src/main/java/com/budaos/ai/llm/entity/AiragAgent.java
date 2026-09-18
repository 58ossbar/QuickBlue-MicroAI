package com.budaos.ai.llm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI智能体实体
 */
@Data
@TableName("airag_agent")
public class AiragAgent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建日期
     */
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新日期
     */
    private LocalDateTime updateTime;

    /**
     * 所属部门
     */
    private String sysOrgCode;

    /**
     * 租户id
     */
    private String tenantId;

    /**
     * 智能体名称
     */
    private String name;

    /**
     * 智能体描述
     */
    private String description;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 系统提示词
     */
    private String systemPrompt;

    /**
     * 关联模型ID
     */
    private String modelId;

    /**
     * 关联知识库ID(逗号分隔)
     */
    private String knowledgeIds;

    /**
     * 关联工具ID(逗号分隔)
     */
    private String toolIds;

    /**
     * 最大迭代次数
     */
    private Integer maxIterations;

    /**
     * 温度参数
     */
    private BigDecimal temperature;

    /**
     * 最大Token数
     */
    private Integer maxTokens;

    /**
     * 状态(draft=草稿,published=已发布,archived=已归档)
     */
    private String status;

    /**
     * 扩展元数据
     */
    private String metadata;

    /**
     * 逻辑删除(0=正常,1=已删除)
     */
    private Integer deletedFlag;
}
