package com.budaos.ai.chat.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.budaos.ai.app.entity.AiragApp;
import com.budaos.ai.app.mapper.AiragAppMapper;
import com.budaos.ai.chat.dto.ChatMessageDTO;
import com.budaos.ai.chat.dto.ChatRequest;
import com.budaos.ai.chat.dto.ChatResponse;
import com.budaos.ai.chat.dto.SseEventDTO;
import com.budaos.ai.chat.factory.ChatModelFactoryManager;
import com.budaos.ai.chat.service.IChatService;
import com.budaos.ai.llm.entity.AiragModel;
import com.budaos.ai.llm.mapper.AiragModelMapper;
import com.budaos.ai.session.entity.AiragMessage;
import com.budaos.ai.session.entity.AiragSession;
import com.budaos.ai.session.service.ISessionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.output.TokenUsage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * AI对话服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements IChatService {

    private final ChatModelFactoryManager modelFactoryManager;
    private final ISessionService sessionService;
    private final AiragModelMapper modelMapper;
    private final AiragAppMapper appMapper;
    private final ObjectMapper objectMapper;

    @Value("${ai.chat.default-model-id:1}")
    private String defaultModelId;

    @Value("${ai.chat.default-history-limit:10}")
    private Integer defaultHistoryLimit;

    @Value("${ai.chat.default-system-prompt:}")
    private String defaultSystemPrompt;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @Override
    public ChatResponse chat(ChatRequest request) {
        long startTime = System.currentTimeMillis();
        String sessionId = request.getSessionId();

        try {
            // 1. 获取或创建会话
            AiragSession session = getOrCreateSession(request);
            sessionId = session.getId();

            // 2. 获取模型配置
            AiragModel model = getChatModelConfig(request);
            
            // 3. 构建消息列表
            List<ChatMessage> messages = buildMessages(request, session, model);
            
            // 4. 创建模型并调用
            ChatModel chatModel = modelFactoryManager.getChatModel(model);
            dev.langchain4j.model.chat.response.ChatResponse response = chatModel.chat(messages);
            
            // 5. 保存用户消息和助手响应
            saveUserMessage(sessionId, request.getPrompt(), request.getUserId());
            String assistantContent = response.aiMessage().text();
            AiragMessage assistantMessage = saveAssistantMessage(
                    sessionId, assistantContent, model.getModelName(), 
                    response.tokenUsage(), System.currentTimeMillis() - startTime);
            
            // 6. 构建响应
            ChatResponse chatResponse = ChatResponse.success(sessionId, assistantContent);
            chatResponse.setMessageId(assistantMessage.getId());
            chatResponse.setModel(model.getModelName());
            chatResponse.setResponseTime(System.currentTimeMillis() - startTime);
            
            if (response.tokenUsage() != null) {
                chatResponse.setInputTokens(response.tokenUsage().inputTokenCount());
                chatResponse.setOutputTokens(response.tokenUsage().outputTokenCount());
                chatResponse.setTotalTokens(response.tokenUsage().totalTokenCount());
            }
            
            return chatResponse;
            
        } catch (Exception e) {
            log.error("对话处理失败: sessionId={}", sessionId, e);
            return ChatResponse.error(sessionId, e.getMessage());
        }
    }

    @Override
    public SseEmitter chatStream(ChatRequest request) {
        // 创建SSE发射器，超时时间30分钟
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
        
        // 使用数组存储sessionId以便在lambda内部修改
        final String[] sessionIdHolder = {request.getSessionId()};
        
        executorService.execute(() -> {
            long startTime = System.currentTimeMillis();
            StringBuilder contentBuilder = new StringBuilder();
            
            try {
                // 1. 获取或创建会话
                AiragSession session = getOrCreateSession(request);
                sessionIdHolder[0] = session.getId();
                String sessionId = sessionIdHolder[0];
                
                // 发送会话ID
                emitter.send(SseEmitter.event()
                        .name("session")
                        .data(sessionId));
                
                // 2. 获取模型配置
                AiragModel model = getChatModelConfig(request);
                
                // 3. 构建消息列表
                List<ChatMessage> messages = buildMessages(request, session, model);
                
                // 4. 保存用户消息
                saveUserMessage(sessionId, request.getPrompt(), request.getUserId());
                
                // 5. 创建流式模型
                StreamingChatModel streamingModel = modelFactoryManager.getStreamingChatModel(model);
                
                // 6. 流式调用
                streamingModel.chat(messages, new StreamingChatResponseHandler() {
                    @Override
                    public void onPartialResponse(String partialResponse) {
                        try {
                            contentBuilder.append(partialResponse);
                            // 发送流式内容
                            emitter.send(SseEmitter.event()
                                    .name("message")
                                    .data(partialResponse));
                        } catch (IOException e) {
                            log.error("SSE发送失败", e);
                        }
                    }

                    @Override
                    public void onCompleteResponse(dev.langchain4j.model.chat.response.ChatResponse response) {
                        try {
                            long responseTime = System.currentTimeMillis() - startTime;
                            
                            // 保存助手消息
                            AiragMessage assistantMessage = saveAssistantMessage(
                                    sessionId, contentBuilder.toString(), 
                                    model.getModelName(),
                                    response.tokenUsage(), responseTime);
                            
                            // 发送完成事件
                            ChatResponse chatResponse = new ChatResponse();
                            chatResponse.setSessionId(sessionId);
                            chatResponse.setMessageId(assistantMessage.getId());
                            chatResponse.setFinished(true);
                            chatResponse.setSuccess(true);
                            chatResponse.setModel(model.getModelName());
                            chatResponse.setResponseTime(responseTime);
                            
                            if (response.tokenUsage() != null) {
                                chatResponse.setInputTokens(response.tokenUsage().inputTokenCount());
                                chatResponse.setOutputTokens(response.tokenUsage().outputTokenCount());
                                chatResponse.setTotalTokens(response.tokenUsage().totalTokenCount());
                                
                                // 发送Token统计
                                emitter.send(SseEmitter.event()
                                        .name("tokens")
                                        .data(objectMapper.writeValueAsString(SseEventDTO.tokens(
                                                response.tokenUsage().inputTokenCount(),
                                                response.tokenUsage().outputTokenCount(),
                                                response.tokenUsage().totalTokenCount()
                                        ).getData())));
                            }
                            
                            // 发送完成标记
                            emitter.send(SseEmitter.event()
                                    .name("done")
                                    .data("[DONE]"));
                            
                            emitter.complete();
                            
                        } catch (Exception e) {
                            log.error("完成响应处理失败", e);
                            try {
                                emitter.send(SseEmitter.event()
                                        .name("error")
                                        .data(e.getMessage()));
                                emitter.completeWithError(e);
                            } catch (IOException ex) {
                                log.error("发送错误事件失败", ex);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                        log.error("流式响应错误", error);
                        try {
                            emitter.send(SseEmitter.event()
                                    .name("error")
                                    .data(error.getMessage()));
                            emitter.completeWithError(error);
                        } catch (IOException e) {
                            log.error("发送错误事件失败", e);
                        }
                    }
                });
                
            } catch (Exception e) {
                log.error("流式对话处理失败: sessionId={}", sessionIdHolder[0], e);
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data(e.getMessage()));
                    emitter.completeWithError(e);
                } catch (IOException ex) {
                    log.error("发送错误事件失败", ex);
                }
            }
        });
        
        // 设置超时和完成回调
        emitter.onTimeout(() -> {
            log.warn("SSE连接超时: sessionId={}", sessionIdHolder[0]);
        });
        
        emitter.onCompletion(() -> {
            log.debug("SSE连接关闭: sessionId={}", sessionIdHolder[0]);
        });
        
        emitter.onError(throwable -> {
            log.error("SSE连接错误: sessionId={}", sessionIdHolder[0], throwable);
        });
        
        return emitter;
    }

    @Override
    public String getCategory() {
        return "default";
    }

    /**
     * 获取或创建会话
     */
    private AiragSession getOrCreateSession(ChatRequest request) {
        if (StrUtil.isNotBlank(request.getSessionId())) {
            AiragSession session = sessionService.getSession(request.getSessionId());
            if (session != null) {
                sessionService.updateActiveTime(session.getId());
                return session;
            }
        }
        
        // 创建新会话
        String title = request.getPrompt();
        if (title != null && title.length() > 20) {
            title = title.substring(0, 20) + "...";
        }
        return sessionService.createSession(request.getAppId(), request.getUserId(), title);
    }

    /**
     * 获取聊天模型配置
     */
    private AiragModel getChatModelConfig(ChatRequest request) {
        // 优先使用请求中的模型ID
        if (StrUtil.isNotBlank(request.getModelId())) {
            AiragModel model = modelMapper.selectById(request.getModelId());
            if (model != null) {
                return model;
            }
        }
        
        // 从应用配置获取模型
        if (StrUtil.isNotBlank(request.getAppId())) {
            AiragApp app = appMapper.selectById(request.getAppId());
            if (app != null && StrUtil.isNotBlank(app.getModelId())) {
                AiragModel model = modelMapper.selectById(app.getModelId());
                if (model != null) {
                    return model;
                }
            }
        }
        
        // 获取默认激活的模型
        AiragModel activeModel = modelMapper.selectOne(
                new LambdaQueryWrapper<AiragModel>()
                        .eq(AiragModel::getActivateFlag, 1)
                        .eq(AiragModel::getModelType, "LLM")
                        .last("LIMIT 1"));
        
        if (activeModel != null) {
            return activeModel;
        }
        
        // 最后使用默认配置ID
        return modelMapper.selectById(defaultModelId);
    }

    /**
     * 构建消息列表
     */
    private List<ChatMessage> buildMessages(ChatRequest request, AiragSession session, AiragModel model) {
        List<ChatMessage> messages = new ArrayList<>();
        
        // 1. 系统提示词
        String systemPrompt = getSystemPrompt(request, model);
        if (StrUtil.isNotBlank(systemPrompt)) {
            messages.add(SystemMessage.from(systemPrompt));
        }
        
        // 2. 历史消息
        int historyLimit = request.getHistoryLimit() != null ? 
                request.getHistoryLimit() : defaultHistoryLimit;
        
        // 如果请求中包含历史消息，使用请求中的
        if (CollUtil.isNotEmpty(request.getMessages())) {
            for (ChatMessageDTO dto : request.getMessages()) {
                ChatMessage msg = convertToChatMessage(dto);
                if (msg != null) {
                    messages.add(msg);
                }
            }
        } else {
            // 从数据库加载历史消息
            List<AiragMessage> historyMessages = sessionService.getRecentMessages(session.getId(), historyLimit);
            for (AiragMessage msg : historyMessages) {
                ChatMessage chatMessage = convertToChatMessage(msg);
                if (chatMessage != null) {
                    messages.add(chatMessage);
                }
            }
        }
        
        // 3. 当前用户消息
        if (StrUtil.isNotBlank(request.getPrompt())) {
            messages.add(UserMessage.from(request.getPrompt()));
        }
        
        return messages;
    }

    /**
     * 获取系统提示词
     */
    private String getSystemPrompt(ChatRequest request, AiragModel model) {
        // 优先级：请求指定 > 应用配置 > 模型配置 > 默认
        if (StrUtil.isNotBlank(request.getSystemPrompt())) {
            return request.getSystemPrompt();
        }
        
        if (StrUtil.isNotBlank(request.getAppId())) {
            AiragApp app = appMapper.selectById(request.getAppId());
            if (app != null && StrUtil.isNotBlank(app.getPrompt())) {
                return app.getPrompt();
            }
        }
        
        return defaultSystemPrompt;
    }

    /**
     * 转换消息DTO
     */
    private ChatMessage convertToChatMessage(ChatMessageDTO dto) {
        if (dto == null || StrUtil.isBlank(dto.getRole())) {
            return null;
        }
        
        return switch (dto.getRole().toLowerCase()) {
            case "system" -> SystemMessage.from(dto.getContent());
            case "user" -> UserMessage.from(dto.getContent());
            case "assistant" -> AiMessage.from(dto.getContent());
            default -> null;
        };
    }

    /**
     * 转换消息实体
     */
    private ChatMessage convertToChatMessage(AiragMessage msg) {
        if (msg == null || StrUtil.isBlank(msg.getRole())) {
            return null;
        }
        
        return switch (msg.getRole().toLowerCase()) {
            case "system" -> SystemMessage.from(msg.getContent());
            case "user" -> UserMessage.from(msg.getContent());
            case "assistant" -> AiMessage.from(msg.getContent());
            default -> null;
        };
    }

    /**
     * 保存用户消息
     */
    private void saveUserMessage(String sessionId, String content, String userId) {
        AiragMessage message = new AiragMessage();
        message.setSessionId(sessionId);
        message.setRole(AiragMessage.Role.USER.getValue());
        message.setContent(content);
        message.setStatus(0);
        sessionService.addMessage(sessionId, message);
    }

    /**
     * 保存助手消息
     */
    private AiragMessage saveAssistantMessage(String sessionId, String content, 
            String modelName, TokenUsage tokenUsage, 
            long responseTime) {
        AiragMessage message = new AiragMessage();
        message.setSessionId(sessionId);
        message.setRole(AiragMessage.Role.ASSISTANT.getValue());
        message.setContent(content);
        message.setModelName(modelName);
        message.setResponseTime(responseTime);
        message.setStatus(0);
        
        if (tokenUsage != null) {
            message.setInputTokens(tokenUsage.inputTokenCount());
            message.setOutputTokens(tokenUsage.outputTokenCount());
            message.setTotalTokens(tokenUsage.totalTokenCount());
        }
        
        return sessionService.addMessage(sessionId, message);
    }
}
