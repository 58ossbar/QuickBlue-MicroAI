package com.budaos.ai.session.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.budaos.ai.session.entity.AiragMessage;
import com.budaos.ai.session.entity.AiragSession;
import com.budaos.ai.session.mapper.AiragSessionMapper;
import com.budaos.ai.session.service.IAiragSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI会话Service实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiragSessionServiceImpl extends ServiceImpl<AiragSessionMapper, AiragSession> implements IAiragSessionService {

    @Override
    public AiragSession getById(String id) {
        return baseMapper.selectById(id);
    }

    @Override
    public List<AiragMessage> getMessagesBySessionId(String sessionId, int limit) {
        // TODO: 实现获取消息列表的逻辑
        // 需要AiragMessageMapper
        return List.of();
    }

    @Override
    public void saveMessage(AiragMessage message) {
        // TODO: 实现保存消息的逻辑
        // 需要AiragMessageMapper
        log.info("保存消息: sessionId={}, role={}, content={}",
                message.getSessionId(), message.getRole(), message.getContent());
    }
}
