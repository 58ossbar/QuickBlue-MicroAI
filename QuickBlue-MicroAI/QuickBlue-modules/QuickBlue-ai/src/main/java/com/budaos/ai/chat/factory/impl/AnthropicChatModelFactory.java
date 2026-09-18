package com.budaos.ai.chat.factory.impl;

import cn.hutool.core.util.StrUtil;
import com.budaos.ai.chat.factory.ChatModelFactory;
import com.budaos.ai.llm.entity.AiragModel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.anthropic.AnthropicStreamingChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;

/**
 * Anthropic Claude模型工厂实现
 */
@Slf4j
@Component
public class AnthropicChatModelFactory implements ChatModelFactory {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ChatModel createChatModel(AiragModel model) {
        Map<String, Object> params = parseModelParams(model.getModelParams());
        
        AnthropicChatModel.AnthropicChatModelBuilder builder = AnthropicChatModel.builder()
                .baseUrl(model.getBaseUrl())
                .apiKey(model.getCredential())
                .modelName(model.getModelName() != null ? model.getModelName() : "claude-3-opus-20240229")
                .timeout(Duration.ofMinutes(5));

        if (params.containsKey("temperature")) {
            builder.temperature(((Number) params.get("temperature")).doubleValue());
        }
        if (params.containsKey("maxTokens")) {
            builder.maxTokens(((Number) params.get("maxTokens")).intValue());
        }
        if (params.containsKey("topP")) {
            builder.topP(((Number) params.get("topP")).doubleValue());
        }

        log.debug("创建Anthropic同步聊天模型: model={}", model.getModelName());
        return builder.build();
    }

    @Override
    public StreamingChatModel createStreamingChatModel(AiragModel model) {
        Map<String, Object> params = parseModelParams(model.getModelParams());
        
        AnthropicStreamingChatModel.AnthropicStreamingChatModelBuilder builder = AnthropicStreamingChatModel.builder()
                .baseUrl(model.getBaseUrl())
                .apiKey(model.getCredential())
                .modelName(model.getModelName() != null ? model.getModelName() : "claude-3-opus-20240229")
                .timeout(Duration.ofMinutes(5));

        if (params.containsKey("temperature")) {
            builder.temperature(((Number) params.get("temperature")).doubleValue());
        }
        if (params.containsKey("maxTokens")) {
            builder.maxTokens(((Number) params.get("maxTokens")).intValue());
        }
        if (params.containsKey("topP")) {
            builder.topP(((Number) params.get("topP")).doubleValue());
        }

        log.debug("创建Anthropic流式聊天模型: model={}", model.getModelName());
        return builder.build();
    }

    @Override
    public EmbeddingModel createEmbeddingModel(AiragModel model) {
        // Anthropic暂不支持Embedding
        log.warn("Anthropic暂不支持Embedding，返回null");
        return null;
    }

    @Override
    public boolean supports(String provider) {
        return "anthropic".equalsIgnoreCase(provider) 
                || "claude".equalsIgnoreCase(provider);
    }

    @Override
    public String getProvider() {
        return "anthropic";
    }

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
}
