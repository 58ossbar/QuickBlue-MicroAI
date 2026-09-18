package com.budaos.ai.chat.factory.impl;

import cn.hutool.core.util.StrUtil;
import com.budaos.ai.chat.factory.ChatModelFactory;
import com.budaos.ai.llm.entity.AiragModel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;

/**
 * Ollama模型工厂实现
 */
@Slf4j
@Component
public class OllamaChatModelFactory implements ChatModelFactory {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ChatModel createChatModel(AiragModel model) {
        Map<String, Object> params = parseModelParams(model.getModelParams());
        
        OllamaChatModel.OllamaChatModelBuilder builder = OllamaChatModel.builder()
                .baseUrl(normalizeBaseUrl(model.getBaseUrl()))
                .modelName(model.getModelName())
                .timeout(Duration.ofMinutes(5));

        // 设置可选参数
        if (params.containsKey("temperature")) {
            builder.temperature(((Number) params.get("temperature")).doubleValue());
        }
        if (params.containsKey("numPredict")) {
            builder.numPredict(((Number) params.get("numPredict")).intValue());
        }

        log.debug("创建Ollama同步聊天模型: model={}", model.getModelName());
        return builder.build();
    }

    @Override
    public StreamingChatModel createStreamingChatModel(AiragModel model) {
        Map<String, Object> params = parseModelParams(model.getModelParams());
        
        OllamaStreamingChatModel.OllamaStreamingChatModelBuilder builder = OllamaStreamingChatModel.builder()
                .baseUrl(normalizeBaseUrl(model.getBaseUrl()))
                .modelName(model.getModelName())
                .timeout(Duration.ofMinutes(5));

        // 设置可选参数
        if (params.containsKey("temperature")) {
            builder.temperature(((Number) params.get("temperature")).doubleValue());
        }
        if (params.containsKey("numPredict")) {
            builder.numPredict(((Number) params.get("numPredict")).intValue());
        }

        log.debug("创建Ollama流式聊天模型: model={}", model.getModelName());
        return builder.build();
    }

    @Override
    public EmbeddingModel createEmbeddingModel(AiragModel model) {
        if (StrUtil.isBlank(model.getModelName())) {
            log.warn("未配置嵌入模型名称，返回null");
            return null;
        }

        return OllamaEmbeddingModel.builder()
                .baseUrl(normalizeBaseUrl(model.getBaseUrl()))
                .modelName(model.getModelName())
                .timeout(Duration.ofMinutes(2))
                .build();
    }

    @Override
    public boolean supports(String provider) {
        return "ollama".equalsIgnoreCase(provider);
    }

    @Override
    public String getProvider() {
        return "ollama";
    }

    /**
     * 解析模型参数
     */
    private Map<String, Object> parseModelParams(String modelParams) {
        if (StrUtil.isBlank(modelParams)) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(modelParams, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.warn("解析模型参数失败: {}", modelParams, e);
            return Map.of();
        }
    }

    /**
     * 标准化BaseURL
     */
    private String normalizeBaseUrl(String baseUrl) {
        if (StrUtil.isBlank(baseUrl)) {
            return "http://localhost:11434";
        }
        // 移除末尾斜杠
        if (baseUrl.endsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }
}
