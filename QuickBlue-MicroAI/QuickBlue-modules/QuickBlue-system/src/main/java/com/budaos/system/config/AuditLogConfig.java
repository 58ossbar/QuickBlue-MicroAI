package com.budaos.system.config;

import com.budaos.system.domain.entity.AuditLogEntity;
import lombok.Builder;
import lombok.Data;

import java.util.function.Function;

/**
 * 操作日志配置
 *
 * @author budaos
 */
@Data
@Builder
public class AuditLogConfig {

    /**
     * 操作日志存储方法
     */
    private Function<AuditLogEntity, Boolean> saveFunction;

    /**
     * 核心线程数
     */
    private Integer corePoolSize;

    /**
     * 队列大小
     */
    private Integer queueCapacity;
}
