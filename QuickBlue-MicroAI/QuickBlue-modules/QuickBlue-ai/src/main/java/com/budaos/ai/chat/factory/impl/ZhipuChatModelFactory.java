package com.budaos.ai.chat.factory.impl;

import cn.hutool.core.util.StrUtil;
import com.budaos.ai.chat.factory.ChatModelFactory;
import com.budaos.ai.llm.entity.AiragModel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.community.model.zhipu.ZhipuAiChatModel;
import dev.langchain4j.community.model.zhipu.ZhipuAiEmbeddingModel;
import dev.langchain4j.community.model.zhipu.ZhipuAiStreamingChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 智谱AI模型工厂实现
 */
@Slf4j
@Component
public class ZhipuChatModelFactory implements ChatModelFactory {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ChatModel createChatModel(AiragModel model) {
        Map<String, Object> params = parseModelParams(model.getModelParams());

        ZhipuAiChatModel.ZhipuAiChatModelBuilder builder = ZhipuAiChatModel.builder()
                .apiKey(model.getCredential())
                .model(model.getModelName() != null ? model.getModelName() : "glm-4");

        // 设置可选参数
        if (params.containsKey("temperature")) {
            builder.temperature(((Number) params.get("temperature")).doubleValue());
        }
        if (params.containsKey("topP")) {
            builder.topP(((Number) params.get("topP")).doubleValue());
        }

        log.debug("创建智谱同步聊天模型: model={}", model.getModelName());
        return builder.build();
    }

    @Override
    public StreamingChatModel createStreamingChatModel(AiragModel model) {
        Map<String, Object> params = parseModelParams(model.getModelParams());

        ZhipuAiStreamingChatModel.ZhipuAiStreamingChatModelBuilder builder = ZhipuAiStreamingChatModel.builder()
                .apiKey(model.getCredential())
                .model(model.getModelName() != null ? model.getModelName() : "glm-4");

        // 设置可选参数
        if (params.containsKey("temperature")) {
            builder.temperature(((Number) params.get("temperature")).doubleValue());
        }
        if (params.containsKey("topP")) {
            builder.topP(((Number) params.get("topP")).doubleValue());
        }

        log.debug("创建智谱流式聊天模型: model={}", model.getModelName());
        return builder.build();
    }

    @Override
    public EmbeddingModel createEmbeddingModel(AiragModel model) {
        if (StrUtil.isBlank(model.getCredential())) {
            log.warn("未配置嵌入模型凭证，返回null");
            return null;
        }

        return ZhipuAiEmbeddingModel.builder()
                .apiKey(model.getCredential())
                .model(model.getModelName() != null ? model.getModelName() : "embedding-2")
                .build();
    }

    @Override
    public boolean supports(String provider) {
        return "zhipu".equalsIgnoreCase(provider);
    }

    @Override
    public String getProvider() {
        return "zhipu";
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
}
