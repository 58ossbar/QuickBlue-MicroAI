package com.budaos.ai.chat.factory;

import com.budaos.ai.llm.entity.AiragModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 模型工厂管理器
 */
@Slf4j
@Component
public class ChatModelFactoryManager {

    private final Map<String, ChatModelFactory> factoryMap = new ConcurrentHashMap<>();

    @Autowired
    public ChatModelFactoryManager(java.util.List<ChatModelFactory> factories) {
        for (ChatModelFactory factory : factories) {
            factoryMap.put(factory.getProvider().toLowerCase(), factory);
            log.info("注册模型工厂: {}", factory.getProvider());
        }
    }

    /**
     * 获取同步聊天模型
     */
    public ChatModel getChatModel(AiragModel model) {
        ChatModelFactory factory = getFactory(model.getProvider());
        return factory.createChatModel(model);
    }

    /**
     * 获取流式聊天模型
     */
    public StreamingChatModel getStreamingChatModel(AiragModel model) {
        ChatModelFactory factory = getFactory(model.getProvider());
        return factory.createStreamingChatModel(model);
    }

    /**
     * 获取嵌入模型
     */
    public EmbeddingModel getEmbeddingModel(AiragModel model) {
        ChatModelFactory factory = getFactory(model.getProvider());
        return factory.createEmbeddingModel(model);
    }

    /**
     * 获取工厂
     */
    private ChatModelFactory getFactory(String provider) {
        if (provider == null) {
            provider = "openai";
        }
        
        ChatModelFactory factory = factoryMap.get(provider.toLowerCase());
        if (factory == null) {
            // 尝试使用OpenAI兼容模式
            factory = factoryMap.get("openai");
            log.warn("未找到模型工厂: {}, 使用OpenAI兼容模式", provider);
        }
        
        if (factory == null) {
            throw new IllegalArgumentException("不支持的模型供应商: " + provider);
        }
        
        return factory;
    }

    /**
     * 检查是否支持该供应商
     */
    public boolean supports(String provider) {
        return factoryMap.containsKey(provider.toLowerCase());
    }

    /**
     * 获取所有支持的供应商
     */
    public java.util.Set<String> getSupportedProviders() {
        return factoryMap.keySet();
    }
}
