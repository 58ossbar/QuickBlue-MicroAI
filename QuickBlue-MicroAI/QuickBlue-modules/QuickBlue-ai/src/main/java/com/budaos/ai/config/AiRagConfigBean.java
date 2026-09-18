package com.budaos.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * AI RAG 配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "ai.rag")
public class AiRagConfigBean {

    /** 向量数据库配置 */
    private EmbedStoreConfig embedStore = new EmbedStoreConfig();

    /** 敏感节点配置 */
    private String allowSensitiveNodes = "";

    @Data
    public static class EmbedStoreConfig {
        /** 主机地址 */
        private String host = "localhost";
        /** 端口 */
        private Integer port = 15432;
        /** 数据库名 */
        private String database = "postgres";
        /** 用户名 */
        private String user = "postgres";
        /** 密码 */
        private String password = "123456";
        /** 表名 */
        private String table = "embeddings";
    }
}
