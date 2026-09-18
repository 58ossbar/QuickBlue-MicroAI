package com.budaos.ai.llm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI工具实体
 */
@Data
@TableName("airag_tool")
public class AiragTool implements Serializable {

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
     * 工具名称
     */
    private String name;

    /**
     * 工具类型(builtin/http/script/mcp/custom)
     */
    private String type;

    /**
     * 工具描述
     */
    private String description;

    /**
     * 输入参数JSON Schema
     */
    private String inputSchema;

    /**
     * 输出参数JSON Schema
     */
    private String outputSchema;

    /**
     * 工具配置
     */
    private String config;

    /**
     * 是否启用(0=禁用,1=启用)
     */
    private Integer enabled;

    /**
     * 逻辑删除(0=正常,1=已删除)
     */
    private Integer deletedFlag;
}
