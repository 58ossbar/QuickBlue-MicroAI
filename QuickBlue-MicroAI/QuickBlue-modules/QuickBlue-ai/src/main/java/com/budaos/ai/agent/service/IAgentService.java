package com.budaos.ai.agent.service;

import com.budaos.ai.agent.dto.AgentExecuteRequest;
import com.budaos.ai.agent.dto.AgentExecuteResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Agent服务接口
 */
public interface IAgentService {

    /**
     * 执行Agent（同步）
     */
    AgentExecuteResponse execute(AgentExecuteRequest request);

    /**
     * 执行Agent（流式）
     */
    SseEmitter executeStream(AgentExecuteRequest request);

    /**
     * 启用Agent
     */
    boolean enableAgent(String agentId);

    /**
     * 禁用Agent
     */
    boolean disableAgent(String agentId);

    /**
     * 发布Agent
     */
    boolean publishAgent(String agentId);
}
