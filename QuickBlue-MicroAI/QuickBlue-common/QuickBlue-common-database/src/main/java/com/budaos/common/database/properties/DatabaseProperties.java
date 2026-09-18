package com.budaos.common.database.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 数据库配置属性
 *
 * @author budaos
 */
@Data
@ConfigurationProperties(prefix = "QuickBlue.database")
public class DatabaseProperties {

    /**
     * 是否启用SQL日志
     */
    private Boolean sqlLogEnabled = true;

    /**
     * 是否启用P6Spy
     */
    private Boolean p6SpyEnabled = true;
}
