package com.budaos.ai.agent.tool;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolSpecification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工具注册中心
 */
@Slf4j
@Component
public class ToolRegistry implements InitializingBean {

    private final Map<String, Object> tools = new ConcurrentHashMap<>();
    private final Map<String, ToolSpecification> toolSpecifications = new ConcurrentHashMap<>();

    @Override
    public void afterPropertiesSet() {
        // 注册内置工具
        registerBuiltInTools();
        log.info("工具注册中心初始化完成，已注册工具数量: {}", tools.size());
    }

    /**
     * 注册内置工具
     */
    private void registerBuiltInTools() {
        // 注册计算器工具
        registerTool("calculator", new CalculatorTool());

        // 注册搜索工具（示例）
        registerTool("web_search", new WebSearchTool());

        // 注册时间工具
        registerTool("current_time", new CurrentTimeTool());

        log.info("内置工具注册完成");
    }

    /**
     * 注册工具
     */
    public void registerTool(String name, Object toolInstance) {
        if (tools.containsKey(name)) {
            log.warn("工具已存在，将被覆盖: {}", name);
        }

        tools.put(name, toolInstance);

        // 提取工具规范
        ToolSpecification spec = extractToolSpecification(toolInstance);
        if (spec != null) {
            toolSpecifications.put(name, spec);
        }

        log.info("工具注册成功: {}, 规范: {}", name, spec != null ? spec.name() : "无");
    }

    /**
     * 获取工具实例
     */
    public Object getTool(String name) {
        return tools.get(name);
    }

    /**
     * 获取所有工具
     */
    public Map<String, Object> getAllTools() {
        return new HashMap<>(tools);
    }

    /**
     * 获取工具规范
     */
    public ToolSpecification getToolSpecification(String name) {
        return toolSpecifications.get(name);
    }

    /**
     * 获取所有工具规范
     */
    public List<ToolSpecification> getAllToolSpecifications() {
        return new ArrayList<>(toolSpecifications.values());
    }

    /**
     * 注销工具
     */
    public void unregisterTool(String name) {
        tools.remove(name);
        toolSpecifications.remove(name);
        log.info("工具已注销: {}", name);
    }

    /**
     * 提取工具规范
     */
    private ToolSpecification extractToolSpecification(Object toolInstance) {
        Class<?> clazz = toolInstance.getClass();

        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Tool.class)) {
                Tool toolAnnotation = method.getAnnotation(Tool.class);

                // 获取工具名称
                String toolName = toolAnnotation.name().isEmpty() ? method.getName() : toolAnnotation.name();

                // 构建工具规范
                ToolSpecification.Builder builder = ToolSpecification.builder()
                        .name(toolName);

                // TODO: 解析参数schema

                return builder.build();
            }
        }

        return null;
    }

    // ==================== 内置工具实现 ====================

    /**
     * 计算器工具
     */
    public static class CalculatorTool {
        @Tool("执行数学计算，支持加减乘除和括号")
        public double calculate(String expression) {
            try {
                // 简化版计算器，实际应用中应使用更安全的计算引擎
                return eval(expression);
            } catch (Exception e) {
                throw new RuntimeException("计算错误: " + e.getMessage());
            }
        }

        private double eval(String expression) {
            // TODO: 实现安全的表达式求值
            return 0.0;
        }
    }

    /**
     * 网络搜索工具（示例）
     */
    public static class WebSearchTool {
        @Tool("在互联网上搜索信息")
        public String search(String query, int limit) {
            // TODO: 集成实际的搜索API（如Google Search API、Bing Search API）
            return "这是搜索[" + query + "]的结果（示例）";
        }
    }

    /**
     * 当前时间工具
     */
    public static class CurrentTimeTool {
        @Tool("获取当前日期和时间")
        public String getCurrentTime() {
            return new Date().toString();
        }
    }
}
