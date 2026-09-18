package com.budaos.ai.chat.service;

import com.budaos.ai.chat.dto.ChatRequest;
import com.budaos.ai.chat.dto.ChatResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AI对话服务接口
 */
public interface IChatService {

    /**
     * 同步对话(非流式)
     *
     * @param request 对话请求
     * @return 对话响应
     */
    ChatResponse chat(ChatRequest request);

    /**
     * 流式对话
     *
     * @param request 对话请求
     * @return SSE发射器
     */
    SseEmitter chatStream(ChatRequest request);

    /**
     * 获取服务类别(用于工厂路由)
     *
     * @return 类别标识
     */
    String getCategory();
}
