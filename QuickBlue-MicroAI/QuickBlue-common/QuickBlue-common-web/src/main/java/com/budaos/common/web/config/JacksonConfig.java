package com.budaos.common.web.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Jackson 3 全局序列化配置
 *
 * <p>Spring Boot 4 默认使用 Jackson 3（tools.jackson）做 HTTP JSON 序列化。
 * 雪花算法主键为 19 位 Long，超出 JavaScript Number 安全整数范围
 * （2^53 - 1 ≈ 9.007e15），前端 JSON.parse 时会发生精度坍缩，
 * 导致不同 ID 变成同一个值（表现为列表所有行渲染成同一条数据）。
 *
 * <p>因此全局将 Long（含基本型 long）序列化为字符串输出；
 * Jackson 3 默认支持 JSON 字符串反序列化为 Long，入参无需处理。
 *
 * <p>同时全局将 LocalDateTime 统一序列化为 yyyy-MM-dd HH:mm（年月日时分），
 * 全框架时间展示格式保持一致；反序列化保持默认（入参时间字段均按原格式提交）。
 *
 * @author budaos
 */
@Slf4j
@Configuration
public class JacksonConfig {

    /**
     * 全局时间展示格式：年月日时分
     */
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Bean
    public JacksonModule longToStringModule() {
        SimpleModule module = new SimpleModule("longToStringModule");
        module.addSerializer(Long.class, ToStringSerializer.instance);
        module.addSerializer(Long.TYPE, ToStringSerializer.instance);
        module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DATETIME_FORMATTER));
        log.info("已启用全局 Long -> String 序列化（防止雪花ID前端精度丢失），LocalDateTime 统一序列化为 yyyy-MM-dd HH:mm");
        return module;
    }
}
