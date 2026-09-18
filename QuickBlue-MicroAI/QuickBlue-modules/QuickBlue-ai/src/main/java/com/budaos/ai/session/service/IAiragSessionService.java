package com.budaos.ai.session.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.budaos.ai.session.entity.AiragSession;
import com.budaos.ai.session.entity.AiragMessage;

import java.util.List;

/**
 * AI会话Service接口
 */
public interface IAiragSessionService extends IService<AiragSession> {

    /**
     * 根据ID获取会话
     */
    AiragSession getById(String id);

    /**
     * 根据会话ID获取消息列表
     *
     * @param sessionId 会话ID
     * @param limit 限制数量
     * @return 消息列表
     */
    List<AiragMessage> getMessagesBySessionId(String sessionId, int limit);

    /**
     * 保存消息
     *
     * @param message 消息对象
     */
    void saveMessage(AiragMessage message);
}
