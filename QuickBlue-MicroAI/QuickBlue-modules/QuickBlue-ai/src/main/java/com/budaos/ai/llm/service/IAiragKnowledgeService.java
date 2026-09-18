package com.budaos.ai.llm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.budaos.ai.llm.entity.AiragKnowledge;

import java.util.List;

/**
 * AI知识库 Service
 */
public interface IAiragKnowledgeService extends IService<AiragKnowledge> {

    /**
     * 获取启用的知识库列表
     */
    List<AiragKnowledge> listEnabled();

    /**
     * 根据类型获取知识库
     */
    List<AiragKnowledge> listByType(String type);

    /**
     * 启用知识库
     */
    boolean enableKnowledge(String id);

    /**
     * 禁用知识库
     */
    boolean disableKnowledge(String id);

    /**
     * 批量删除知识库
     */
    boolean removeByIds(List<?> list);

    /**
     * 检查是否被应用引用
     */
    boolean existsReferencedByApp(String knowledgeId);
}
