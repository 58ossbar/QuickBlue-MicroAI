package com.budaos.ai.chat.factory.impl;

import cn.hutool.core.util.StrUtil;
import com.budaos.ai.chat.factory.ChatModelFactory;
import com.budaos.ai.llm.entity.AiragModel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.community.model.qianfan.QianfanChatModel;
import dev.langchain4j.community.model.qianfan.QianfanEmbeddingModel;
import dev.langchain4j.community.model.qianfan.QianfanStreamingChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 百度千帆模型工厂实现
 */
@Slf4j
@Component
public class QianfanChatModelFactory implements ChatModelFactory {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ChatModel createChatModel(AiragModel model) {
        Map<String, Object> params = parseModelParams(model.getModelParams());
        
        // credential格式: apiKey,secretKey
        String[] keys = parseCredential(model.getCredential());
        String apiKey = keys[0];
        String secretKey = keys.length > 1 ? keys[1] : "";
        
        QianfanChatModel.QianfanChatModelBuilder builder = QianfanChatModel.builder()
                .apiKey(apiKey)
                .secretKey(secretKey)
                .modelName(model.getModelName() != null ? model.getModelName() : "ERNIE-Bot-4");

        if (params.containsKey("temperature")) {
            builder.temperature(((Number) params.get("temperature")).doubleValue());
        }
        if (params.containsKey("topP")) {
            builder.topP(((Number) params.get("topP")).doubleValue());
        }

        log.debug("创建百度千帆同步聊天模型: model={}", model.getModelName());
        return builder.build();
    }

    @Override
    public StreamingChatModel createStreamingChatModel(AiragModel model) {
        Map<String, Object> params = parseModelParams(model.getModelParams());
        
        // credential格式: apiKey,secretKey
        String[] keys = parseCredential(model.getCredential());
        String apiKey = keys[0];
        String secretKey = keys.length > 1 ? keys[1] : "";
        
        QianfanStreamingChatModel.QianfanStreamingChatModelBuilder builder = QianfanStreamingChatModel.builder()
                .apiKey(apiKey)
                .secretKey(secretKey)
                .modelName(model.getModelName() != null ? model.getModelName() : "ERNIE-Bot-4");

        if (params.containsKey("temperature")) {
            builder.temperature(((Number) params.get("temperature")).doubleValue());
        }
        if (params.containsKey("topP")) {
            builder.topP(((Number) params.get("topP")).doubleValue());
        }

        log.debug("创建百度千帆流式聊天模型: model={}", model.getModelName());
        return builder.build();
    }

    @Override
    public EmbeddingModel createEmbeddingModel(AiragModel model) {
        if (StrUtil.isBlank(model.getCredential())) {
            log.warn("未配置嵌入模型凭证，返回null");
            return null;
        }

        String[] keys = parseCredential(model.getCredential());
        String apiKey = keys[0];
        String secretKey = keys.length > 1 ? keys[1] : "";

        return QianfanEmbeddingModel.builder()
                .apiKey(apiKey)
                .secretKey(secretKey)
                .modelName(model.getModelName() != null ? model.getModelName() : "Embedding-V1")
                .build();
    }

    @Override
    public boolean supports(String provider) {
        return "qianfan".equalsIgnoreCase(provider) 
                || "baidu".equalsIgnoreCase(provider)
                || "ernie".equalsIgnoreCase(provider);
    }

    @Override
    public String getProvider() {
        return "qianfan";
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

    /**
     * 解析凭证，支持逗号分隔或JSON格式
     */
    private String[] parseCredential(String credential) {
        if (StrUtil.isBlank(credential)) {
            return new String[]{"", ""};
        }
        // 简单的逗号分隔格式: apiKey,secretKey
        if (credential.contains(",")) {
            return credential.split(",", 2);
        }
        return new String[]{credential, ""};
    }
}
