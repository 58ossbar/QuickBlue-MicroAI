package com.budaos.ai.api.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * AI 应用 DTO
 */
@Data
public class AiAppDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 主键 */
    private String id;
    
    /** 应用名称 */
    private String name;
    
    /** 应用图标 */
    private String icon;
    
    /** 应用描述 */
    private String description;
    
    /** 应用类型: 1-普通聊天 2-流程应用 */
    private Integer type;
    
    /** 关联流程ID */
    private String flowId;
    
    /** 模型ID */
    private String modelId;
    
    /** 提示词 */
    private String systemPrompt;
    
    /** 状态: 0-禁用 1-启用 */
    private Integer status;
    
    /** 是否公开: 0-否 1-是 */
    private Integer isPublic;
    
    /** 关联知识库ID */
    private String knowledgeId;
}
