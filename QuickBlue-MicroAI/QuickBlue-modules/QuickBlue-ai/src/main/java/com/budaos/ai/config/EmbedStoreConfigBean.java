package com.budaos.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 向量存储配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "ai.rag.embed-store")
public class EmbedStoreConfigBean {

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

    /**
     * 获取JDBC URL
     */
    public String getJdbcUrl() {
        return String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
    }
}
