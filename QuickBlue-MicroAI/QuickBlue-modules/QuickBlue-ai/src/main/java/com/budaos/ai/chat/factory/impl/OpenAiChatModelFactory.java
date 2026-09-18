package com.budaos.ai.chat.factory.impl;

import cn.hutool.core.util.StrUtil;
import com.budaos.ai.chat.factory.ChatModelFactory;
import com.budaos.ai.llm.entity.AiragModel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;

/**
 * OpenAI模型工厂实现
 * 支持: OpenAI, DeepSeek, 及其他OpenAI兼容API
 */
@Slf4j
@Component
public class OpenAiChatModelFactory implements ChatModelFactory {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ChatModel createChatModel(AiragModel model) {
        Map<String, Object> params = parseModelParams(model.getModelParams());
        
        OpenAiChatModel.OpenAiChatModelBuilder builder = OpenAiChatModel.builder()
                .baseUrl(normalizeBaseUrl(model.getBaseUrl()))
                .apiKey(model.getCredential())
                .modelName(model.getModelName())
                .timeout(Duration.ofMinutes(5));

        // 设置可选参数
        if (params.containsKey("temperature")) {
            builder.temperature(((Number) params.get("temperature")).doubleValue());
        }
        if (params.containsKey("maxTokens")) {
            builder.maxTokens(((Number) params.get("maxTokens")).intValue());
        }
        if (params.containsKey("topP")) {
            builder.topP(((Number) params.get("topP")).doubleValue());
        }

        log.debug("创建OpenAI同步聊天模型: provider={}, model={}", model.getProvider(), model.getModelName());
        return builder.build();
    }

    @Override
    public StreamingChatModel createStreamingChatModel(AiragModel model) {
        Map<String, Object> params = parseModelParams(model.getModelParams());
        
        OpenAiStreamingChatModel.OpenAiStreamingChatModelBuilder builder = OpenAiStreamingChatModel.builder()
                .baseUrl(normalizeBaseUrl(model.getBaseUrl()))
                .apiKey(model.getCredential())
                .modelName(model.getModelName())
                .timeout(Duration.ofMinutes(5));

        // 设置可选参数
        if (params.containsKey("temperature")) {
            builder.temperature(((Number) params.get("temperature")).doubleValue());
        }
        if (params.containsKey("maxTokens")) {
            builder.maxTokens(((Number) params.get("maxTokens")).intValue());
        }
        if (params.containsKey("topP")) {
            builder.topP(((Number) params.get("topP")).doubleValue());
        }

        log.debug("创建OpenAI流式聊天模型: provider={}, model={}", model.getProvider(), model.getModelName());
        return builder.build();
    }

    @Override
    public EmbeddingModel createEmbeddingModel(AiragModel model) {
        if (StrUtil.isBlank(model.getCredential())) {
            log.warn("未配置嵌入模型凭证，返回null");
            return null;
        }

        return OpenAiEmbeddingModel.builder()
                .baseUrl(normalizeBaseUrl(model.getBaseUrl()))
                .apiKey(model.getCredential())
                .modelName(model.getModelName() != null ? model.getModelName() : "text-embedding-ada-002")
                .timeout(Duration.ofMinutes(1))
                .build();
    }

    @Override
    public boolean supports(String provider) {
        return "openai".equalsIgnoreCase(provider) 
                || "deepseek".equalsIgnoreCase(provider)
                || "moonshot".equalsIgnoreCase(provider)
                || "yi".equalsIgnoreCase(provider)
                || "baichuan".equalsIgnoreCase(provider)
                || "minimax".equalsIgnoreCase(provider);
    }

    @Override
    public String getProvider() {
        return "openai";
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
            return "https://api.openai.com/v1";
        }
        // 确保URL以/v1结尾
        if (!baseUrl.endsWith("/v1") && !baseUrl.endsWith("/v1/")) {
            if (baseUrl.endsWith("/")) {
                return baseUrl + "v1";
            }
            return baseUrl + "/v1";
        }
        return baseUrl;
    }
}
