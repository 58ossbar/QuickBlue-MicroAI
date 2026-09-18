package com.budaos.ai.api.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * AI 知识库 DTO
 */
@Data
public class AiKnowledgeDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 主键 */
    private String id;
    
    /** 知识库名称 */
    private String name;
    
    /** 知识库描述 */
    private String description;
    
    /** 向量模型ID */
    private String embeddingModelId;
    
    /** 分片大小 */
    private Integer chunkSize;
    
    /** 分片重叠 */
    private Integer chunkOverlap;
    
    /** 状态: 0-禁用 1-启用 */
    private Integer status;
}
