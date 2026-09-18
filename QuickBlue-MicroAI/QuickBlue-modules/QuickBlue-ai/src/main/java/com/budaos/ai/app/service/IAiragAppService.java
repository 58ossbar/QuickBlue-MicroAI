package com.budaos.ai.app.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.budaos.ai.app.entity.AiragApp;

import java.util.List;

/**
 * AI应用 Service
 */
public interface IAiragAppService extends IService<AiragApp> {

    /**
     * 根据ID获取应用
     */
    AiragApp getById(String id);

    /**
     * 保存应用
     */
    boolean saveApp(AiragApp app);

    /**
     * 更新应用
     */
    boolean updateApp(AiragApp app);

    /**
     * 删除应用
     */
    boolean deleteApp(String id);

    /**
     * 批量删除应用
     */
    boolean batchDelete(List<String> ids);

    /**
     * 发布应用
     */
    boolean publishApp(String id);

    /**
     * 禁用应用
     */
    boolean disableApp(String id);

    /**
     * 启用应用
     */
    boolean enableApp(String id);

    /**
     * 检查是否被模型引用
     */
    boolean existsByModelId(String modelId);

    /**
     * 检查是否被知识库引用
     */
    boolean existsByKnowledgeId(String knowledgeId);
}
