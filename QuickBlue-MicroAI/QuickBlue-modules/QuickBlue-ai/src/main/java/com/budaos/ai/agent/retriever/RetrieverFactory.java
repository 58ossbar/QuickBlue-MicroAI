package com.budaos.ai.agent.retriever;

import com.budaos.ai.config.AiRagConfigBean;
import com.budaos.ai.llm.entity.AiragKnowledge;
import com.budaos.ai.llm.entity.AiragModel;
import com.budaos.ai.llm.service.IAiragModelService;
import com.budaos.common.core.exception.BizException;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * RAG检索器工厂
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RetrieverFactory {

    private final AiRagConfigBean aiRagConfig;
    private final IAiragModelService modelService;

    /**
     * 创建检索器
     */
    public SimpleRetriever createRetriever(List<AiragKnowledge> knowledgeBases) {
        if (knowledgeBases == null || knowledgeBases.isEmpty()) {
            throw new BizException("至少需要关联一个知识库");
        }

        // 使用第一个知识库的向量模型
        AiragKnowledge primaryKnowledge = knowledgeBases.get(0);
        AiragModel embedModel = modelService.getById(primaryKnowledge.getEmbedId());

        // 创建嵌入模型
        EmbeddingModel embeddingModel = createEmbeddingModel(embedModel);

        // 创建向量存储
        EmbeddingStore<TextSegment> embeddingStore = createEmbeddingStore();

        return new SimpleRetriever(embeddingModel, embeddingStore, knowledgeBases);
    }

    /**
     * 创建嵌入模型
     */
    private EmbeddingModel createEmbeddingModel(AiragModel model) {
        if (model == null) {
            throw new BizException("向量模型配置不存在");
        }

        // 根据供应商创建对应的嵌入模型
        switch (model.getProvider()) {
            case "openai":
                return OpenAiEmbeddingModel.builder()
                        .baseUrl(model.getBaseUrl())
                        .apiKey(model.getCredential())
                        .modelName(model.getModelName())
                        .build();

            case "ollama":
                return dev.langchain4j.model.ollama.OllamaEmbeddingModel.builder()
                        .baseUrl(model.getBaseUrl())
                        .modelName(model.getModelName())
                        .build();

            default:
                throw new BizException("不支持的向量模型供应商: " + model.getProvider());
        }
    }

    /**
     * 创建向量存储
     */
    private EmbeddingStore<TextSegment> createEmbeddingStore() {
        AiRagConfigBean.EmbedStoreConfig config = aiRagConfig.getEmbedStore();

        return PgVectorEmbeddingStore.builder()
                .host(config.getHost())
                .port(config.getPort())
                .database(config.getDatabase())
                .user(config.getUser())
                .password(config.getPassword())
                .table(config.getTable())
                .build();
    }

    /**
     * 创建文档摄取器
     */
    public EmbeddingStoreIngestor createIngestor(List<AiragKnowledge> knowledgeBases) {
        AiragKnowledge primaryKnowledge = knowledgeBases.get(0);
        AiragModel embedModel = modelService.getById(primaryKnowledge.getEmbedId());

        EmbeddingModel embeddingModel = createEmbeddingModel(embedModel);
        EmbeddingStore<TextSegment> embeddingStore = createEmbeddingStore();

        // 创建文档分割器
        DocumentSplitter splitter = DocumentSplitters.recursive(500, 50);

        return EmbeddingStoreIngestor.builder()
                .documentSplitter(splitter)
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
    }

    /**
     * 简单检索器实现
     */
    public static class SimpleRetriever {
        private final EmbeddingModel embeddingModel;
        private final EmbeddingStore<TextSegment> embeddingStore;
        private final List<AiragKnowledge> knowledgeBases;

        public SimpleRetriever(EmbeddingModel embeddingModel, EmbeddingStore<TextSegment> embeddingStore, List<AiragKnowledge> knowledgeBases) {
            this.embeddingModel = embeddingModel;
            this.embeddingStore = embeddingStore;
            this.knowledgeBases = knowledgeBases;
        }

        /**
         * 检索相关文档
         */
        public List<TextSegment> retrieve(String query, int maxResults, double minScore) {
            // TODO: 实现实际的检索逻辑
            log.info("执行RAG检索, Query: {}, MaxResults: {}, MinScore: {}", query, maxResults, minScore);

            // 生成查询向量
            // dev.langchain4j.model.output.Response<dev.langchain4j.model.output.Embedding> embeddingResponse = embeddingModel.embed(query);

            // 搜索相似文档
            // List<dev.langchain4j.store.embedding.EmbeddingMatch<TextSegment>> matches = embeddingStore.findRelevant(
            //     embeddingResponse.content(), maxResults, minScore
            // );

            // 返回匹配的文档片段
            // return matches.stream()
            //     .map(EmbeddingMatch::embedded)
            //     .collect(Collectors.toList());

            return List.of();
        }

        /**
         * 添加文档到知识库
         */
        public void ingestDocument(Document document) {
            log.info("添加文档到知识库: {}", document.text().substring(0, Math.min(100, document.text().length())));

            // TODO: 实现文档摄取逻辑
        }
    }
}
