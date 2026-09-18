package com.budaos.ai.chat.factory;

import com.budaos.ai.llm.entity.AiragModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;

/**
 * AI模型工厂接口
 */
public interface ChatModelFactory {

    /**
     * 创建同步聊天模型
     *
     * @param model 模型配置
     * @return 同步聊天模型
     */
    ChatModel createChatModel(AiragModel model);

    /**
     * 创建流式聊天模型
     *
     * @param model 模型配置
     * @return 流式聊天模型
     */
    StreamingChatModel createStreamingChatModel(AiragModel model);

    /**
     * 创建嵌入模型
     *
     * @param model 模型配置
     * @return 嵌入模型
     */
    EmbeddingModel createEmbeddingModel(AiragModel model);

    /**
     * 是否支持该供应商
     *
     * @param provider 供应商标识
     * @return 是否支持
     */
    boolean supports(String provider);

    /**
     * 获取供应商标识
     *
     * @return 供应商标识
     */
    String getProvider();
}
