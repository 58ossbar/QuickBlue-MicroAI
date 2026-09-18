package com.budaos.ai.llm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.budaos.ai.llm.entity.AiragModel;

import java.util.List;

/**
 * AI模型 Service
 */
public interface IAiragModelService extends IService<AiragModel> {

    /**
     * 获取已激活的模型列表
     */
    List<AiragModel> listActivated();

    /**
     * 根据供应者获取模型
     */
    List<AiragModel> listByProvider(String provider);

    /**
     * 获取默认模型
     */
    AiragModel getDefaultModel();
}
