package com.budaos.ai.agent.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.budaos.ai.agent.dto.AgentExecuteRequest;
import com.budaos.ai.agent.dto.AgentExecuteResponse;
import com.budaos.ai.agent.service.IAgentService;
import com.budaos.ai.chat.factory.ChatModelFactoryManager;
import com.budaos.ai.llm.entity.AiragAgent;
import com.budaos.ai.llm.entity.AiragKnowledge;
import com.budaos.ai.llm.mapper.AiragAgentMapper;
import com.budaos.ai.llm.service.IAiragKnowledgeService;
import com.budaos.ai.llm.service.IAiragModelService;
import com.budaos.ai.session.entity.AiragSession;
import com.budaos.ai.session.entity.AiragMessage;
import com.budaos.ai.session.service.IAiragSessionService;
import com.budaos.common.core.exception.BizException;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.*;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.output.TokenUsage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Agent服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentServiceImpl implements IAgentService {

    private final AiragAgentMapper agentMapper;
    private final ChatModelFactoryManager modelFactoryManager;
    private final IAiragKnowledgeService knowledgeService;
    private final IAiragModelService modelService;
    private final IAiragSessionService sessionService;

    private final ExecutorService asyncExecutor = Executors.newCachedThreadPool();

    @Override
    public AgentExecuteResponse execute(AgentExecuteRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("开始执行Agent, Agent ID: {}, Input: {}", request.getAgentId(), request.getInput());

        // 1. 加载Agent配置
        AiragAgent agent = loadAgent(request.getAgentId());

        // 2. 加载会话
        AiragSession session = loadOrCreateSession(request);

        // 3. 构建上下文
        AgentContext context = buildContext(agent, request, session);

        // 4. 执行Agent推理
        AgentExecuteResponse response = executeAgent(context);

        // 5. 保存消息
        saveMessages(session, request.getInput(), response.getAnswer());

        // 6. 统计耗时
        response.setDuration(System.currentTimeMillis() - startTime);

        log.info("Agent执行完成, Agent ID: {}, 耗时: {}ms", request.getAgentId(), response.getDuration());
        return response;
    }

    @Override
    public SseEmitter executeStream(AgentExecuteRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("开始执行Agent流式响应, Agent ID: {}, Input: {}", request.getAgentId(), request.getInput());

        // 创建SSE发射器
        SseEmitter emitter = new SseEmitter(60000L);

        asyncExecutor.execute(() -> {
            try {
                // 1. 加载Agent配置
                AiragAgent agent = loadAgent(request.getAgentId());

                // 2. 加载会话
                AiragSession session = loadOrCreateSession(request);

                // 3. 构建上下文
                AgentContext context = buildContext(agent, request, session);

                // 4. 流式执行
                AgentExecuteResponse response = executeAgentStream(context, emitter);

                // 5. 保存消息
                saveMessages(session, request.getInput(), response.getAnswer());

                // 6. 发送完成事件
                response.setDuration(System.currentTimeMillis() - startTime);
                emitter.send(SseEmitter.event().name("complete").data(response));
                emitter.complete();

                log.info("Agent流式执行完成, Agent ID: {}, 耗时: {}ms", request.getAgentId(), response.getDuration());
            } catch (Exception e) {
                log.error("Agent流式执行失败", e);
                try {
                    emitter.send(SseEmitter.event().name("error").data(e.getMessage()));
                    emitter.completeWithError(e);
                } catch (IOException ioException) {
                    log.error("发送错误消息失败", ioException);
                }
            }
        });

        return emitter;
    }

    @Override
    public boolean enableAgent(String agentId) {
        log.info("启用Agent, Agent ID: {}", agentId);

        AiragAgent agent = loadAgent(agentId);
        agent.setStatus("published");
        agent.setUpdateTime(LocalDateTime.now());

        int result = agentMapper.updateById(agent);
        if (result > 0) {
            log.info("Agent启用成功, Agent ID: {}", agentId);
            return true;
        }
        return false;
    }

    @Override
    public boolean disableAgent(String agentId) {
        log.info("禁用Agent, Agent ID: {}", agentId);

        AiragAgent agent = loadAgent(agentId);
        agent.setStatus("draft");
        agent.setUpdateTime(LocalDateTime.now());

        int result = agentMapper.updateById(agent);
        if (result > 0) {
            log.info("Agent禁用成功, Agent ID: {}", agentId);
            return true;
        }
        return false;
    }

    @Override
    public boolean publishAgent(String agentId) {
        log.info("发布Agent, Agent ID: {}", agentId);

        AiragAgent agent = loadAgent(agentId);

        // 验证Agent配置
        validateAgent(agent);

        agent.setStatus("published");
        agent.setUpdateTime(LocalDateTime.now());

        int result = agentMapper.updateById(agent);
        if (result > 0) {
            log.info("Agent发布成功, Agent ID: {}", agentId);
            return true;
        }
        return false;
    }

    // ==================== 私有方法 ====================

    /**
     * 加载Agent配置
     */
    private AiragAgent loadAgent(String agentId) {
        AiragAgent agent = agentMapper.selectById(agentId);
        if (agent == null) {
            throw new BizException("Agent不存在, ID: " + agentId);
        }
        return agent;
    }

    /**
     * 加载或创建会话
     */
    private AiragSession loadOrCreateSession(AgentExecuteRequest request) {
        if (request.getSessionId() != null && !request.getSessionId().isEmpty()) {
            return sessionService.getById(request.getSessionId());
        }

        // 创建新会话
        AiragSession session = new AiragSession();
        session.setAppId(request.getAgentId()); // 使用appId字段存储agentId
        session.setUserId(getCurrentUserId());
        session.setTenantId(getCurrentTenantId());
        session.setCreateTime(new Date());
        session.setUpdateTime(new Date());

        sessionService.save(session);
        return session;
    }

    /**
     * 构建Agent上下文
     */
    private AgentContext buildContext(AiragAgent agent, AgentExecuteRequest request, AiragSession session) {
        AgentContext context = new AgentContext();

        context.setAgent(agent);
        context.setSession(session);
        context.setInput(request.getInput());

        // 加载LLM模型
        ChatModel chatModel = modelFactoryManager.getChatModel(
                modelService.getById(agent.getModelId())
        );
        context.setChatModel(chatModel);

        // 加载知识库
        if (agent.getKnowledgeIds() != null && !agent.getKnowledgeIds().isEmpty()) {
            List<String> knowledgeIdList = Arrays.asList(agent.getKnowledgeIds().split(","));
            context.setKnowledgeBases(knowledgeService.listByIds(knowledgeIdList));
        }

        // 加载工具
        if (agent.getToolIds() != null && !agent.getToolIds().isEmpty()) {
            List<ToolSpecification> tools = loadTools(agent.getToolIds());
            context.setTools(tools);
        }

        // 加载历史消息
        List<AiragMessage> historyMessages = sessionService.getMessagesBySessionId(
                session.getId(), 10
        );
        context.setHistoryMessages(historyMessages);

        return context;
    }

    /**
     * 执行Agent推理（同步）
     */
    private AgentExecuteResponse executeAgent(AgentContext context) {
        AgentExecuteResponse response = new AgentExecuteResponse();
        response.setAgentId(context.getAgent().getId());
        response.setSessionId(context.getSession().getId());

        // 构建消息列表
        List<ChatMessage> messages = buildMessages(context);

        // 调用LLM
        ChatResponse chatResponse = context.getChatModel().chat(messages);
        response.setAnswer(chatResponse.aiMessage().text());

        // Token统计
        if (chatResponse.tokenUsage() != null) {
            AgentExecuteResponse.TokenStats tokenStats = new AgentExecuteResponse.TokenStats();
            tokenStats.setInputTokens(chatResponse.tokenUsage().inputTokenCount());
            tokenStats.setOutputTokens(chatResponse.tokenUsage().outputTokenCount());
            tokenStats.setTotalTokens(chatResponse.tokenUsage().totalTokenCount());
            response.setTokenStats(tokenStats);
        }

        // 推理步骤
        response.setReasoningSteps(new ArrayList<>());
        AgentExecuteResponse.ReasoningStep step = new AgentExecuteResponse.ReasoningStep();
        step.setStepNumber(1);
        step.setStepType("thought");
        step.setContent("根据系统提示词和用户输入生成回答");
        response.getReasoningSteps().add(step);

        return response;
    }

    /**
     * 执行Agent推理（流式）
     */
    private AgentExecuteResponse executeAgentStream(AgentContext context, SseEmitter emitter) throws IOException {
        AgentExecuteResponse response = new AgentExecuteResponse();
        response.setAgentId(context.getAgent().getId());
        response.setSessionId(context.getSession().getId());

        // 构建消息列表
        List<ChatMessage> messages = buildMessages(context);

        // 流式调用
        StreamingChatModel streamingModel = modelFactoryManager.getStreamingChatModel(
                modelService.getById(context.getAgent().getModelId())
        );

        StringBuilder answerBuilder = new StringBuilder();

        streamingModel.chat(messages, new StreamingChatResponseHandler() {
            @Override
            public void onPartialResponse(String partialResponse) {
                try {
                    answerBuilder.append(partialResponse);
                    emitter.send(SseEmitter.event().name("token").data(partialResponse));
                } catch (IOException e) {
                    log.error("发送Token失败", e);
                }
            }

            @Override
            public void onCompleteResponse(ChatResponse chatResponse) {
                response.setAnswer(answerBuilder.toString());

                // 设置Token使用统计
                if (chatResponse.tokenUsage() != null) {
                    AgentExecuteResponse.TokenStats tokenStats = new AgentExecuteResponse.TokenStats();
                    tokenStats.setInputTokens(chatResponse.tokenUsage().inputTokenCount());
                    tokenStats.setOutputTokens(chatResponse.tokenUsage().outputTokenCount());
                    tokenStats.setTotalTokens(chatResponse.tokenUsage().totalTokenCount());
                    response.setTokenStats(tokenStats);
                }

                // 发送完成标记
                try {
                    emitter.send(SseEmitter.event().name("done").data("[DONE]"));
                    emitter.complete();
                } catch (IOException e) {
                    log.error("发送完成标记失败", e);
                }
            }

            @Override
            public void onError(Throwable error) {
                log.error("流式生成失败", error);
                response.setSuccess(false);
                response.setError(error.getMessage());
                try {
                    emitter.completeWithError(error);
                } catch (Exception e) {
                    log.error("发送错误失败", e);
                }
            }
        });

        return response;
    }

    /**
     * 构建消息列表
     */
    private List<ChatMessage> buildMessages(AgentContext context) {
        List<ChatMessage> messages = new ArrayList<>();

        // 系统提示词
        if (context.getAgent().getSystemPrompt() != null && !context.getAgent().getSystemPrompt().isEmpty()) {
            messages.add(SystemMessage.from(context.getAgent().getSystemPrompt()));
        }

        // 历史消息
        if (context.getHistoryMessages() != null) {
            for (AiragMessage msg : context.getHistoryMessages()) {
                if ("user".equals(msg.getRole())) {
                    messages.add(UserMessage.from(msg.getContent()));
                } else if ("assistant".equals(msg.getRole())) {
                    messages.add(AiMessage.from(msg.getContent()));
                }
            }
        }

        // 当前用户输入
        messages.add(UserMessage.from(context.getInput()));

        return messages;
    }

    /**
     * 加载工具
     */
    private List<ToolSpecification> loadTools(String toolIds) {
        // TODO: 实现工具加载逻辑
        return new ArrayList<>();
    }

    /**
     * 验证Agent配置
     */
    private void validateAgent(AiragAgent agent) {
        if (agent.getModelId() == null || agent.getModelId().isEmpty()) {
            throw new BizException("Agent必须配置LLM模型");
        }

        if (agent.getSystemPrompt() == null || agent.getSystemPrompt().isEmpty()) {
            throw new BizException("Agent必须配置系统提示词");
        }
    }

    /**
     * 保存消息
     */
    private void saveMessages(AiragSession session, String userInput, String assistantResponse) {
        // 保存用户消息
        AiragMessage userMessage = new AiragMessage();
        userMessage.setSessionId(session.getId());
        userMessage.setRole("user");
        userMessage.setContent(userInput);
        userMessage.setCreateTime(new Date());
        sessionService.saveMessage(userMessage);

        // 保存助手消息
        AiragMessage assistantMessage = new AiragMessage();
        assistantMessage.setSessionId(session.getId());
        assistantMessage.setRole("assistant");
        assistantMessage.setContent(assistantResponse);
        assistantMessage.setCreateTime(new Date());
        sessionService.saveMessage(assistantMessage);
    }

    /**
     * 获取当前用户ID
     */
    private String getCurrentUserId() {
        try {
            return StpUtil.getLoginIdAsString();
        } catch (Exception e) {
            log.warn("获取用户ID失败", e);
            return "system";
        }
    }

    /**
     * 获取当前租户ID
     */
    private String getCurrentTenantId() {
        try {
            String tenantId = StpUtil.getSession().getString("tenantId");
            return tenantId != null ? tenantId : getCurrentUserId();
        } catch (Exception e) {
            log.warn("获取租户ID失败", e);
            return "system";
        }
    }

    /**
     * Agent上下文
     */
    private class AgentContext {
        private AiragAgent agent;
        private AiragSession session;
        private String input;
        private ChatModel chatModel;
        private List<AiragKnowledge> knowledgeBases;
        private List<ToolSpecification> tools;
        private List<AiragMessage> historyMessages;

        // getters and setters
        public AiragAgent getAgent() { return agent; }
        public void setAgent(AiragAgent agent) { this.agent = agent; }
        public AiragSession getSession() { return session; }
        public void setSession(AiragSession session) { this.session = session; }
        public String getInput() { return input; }
        public void setInput(String input) { this.input = input; }
        public ChatModel getChatModel() { return chatModel; }
        public void setChatModel(ChatModel chatModel) { this.chatModel = chatModel; }
        public List<AiragKnowledge> getKnowledgeBases() { return knowledgeBases; }
        public void setKnowledgeBases(List<AiragKnowledge> knowledgeBases) { this.knowledgeBases = knowledgeBases; }
        public List<ToolSpecification> getTools() { return tools; }
        public void setTools(List<ToolSpecification> tools) { this.tools = tools; }
        public List<AiragMessage> getHistoryMessages() { return historyMessages; }
        public void setHistoryMessages(List<AiragMessage> historyMessages) { this.historyMessages = historyMessages; }
    }
}
