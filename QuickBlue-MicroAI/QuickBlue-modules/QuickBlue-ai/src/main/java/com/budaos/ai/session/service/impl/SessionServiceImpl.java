package com.budaos.ai.session.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.budaos.ai.session.entity.AiragMessage;
import com.budaos.ai.session.entity.AiragSession;
import com.budaos.ai.session.mapper.AiragMessageMapper;
import com.budaos.ai.session.mapper.AiragSessionMapper;
import com.budaos.ai.session.service.ISessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * AI会话服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionServiceImpl extends ServiceImpl<AiragSessionMapper, AiragSession> implements ISessionService {

    private final AiragMessageMapper messageMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiragSession createSession(String appId, String userId, String title) {
        AiragSession session = new AiragSession();
        session.setAppId(appId);
        session.setUserId(userId);
        session.setTitle(StrUtil.isBlank(title) ? "新会话" : title);
        session.setMsgCount(0);
        session.setTotalTokens(0);
        session.setStatus(0);
        session.setDelFlag(0);
        session.setCreateTime(new Date());
        session.setLastActiveTime(new Date());
        
        this.save(session);
        log.info("创建新会话: sessionId={}, userId={}, appId={}", session.getId(), userId, appId);
        return session;
    }

    @Override
    public Page<AiragSession> getUserSessions(String userId, String appId, int pageNum, int pageSize) {
        LambdaQueryWrapper<AiragSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiragSession::getUserId, userId)
                .eq(AiragSession::getDelFlag, 0)
                .eq(StrUtil.isNotBlank(appId), AiragSession::getAppId, appId)
                .orderByDesc(AiragSession::getLastActiveTime);
        
        return this.page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public AiragSession getSession(String sessionId) {
        return this.getById(sessionId);
    }

    @Override
    public void updateActiveTime(String sessionId) {
        LambdaUpdateWrapper<AiragSession> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AiragSession::getId, sessionId)
                .set(AiragSession::getLastActiveTime, new Date());
        this.update(wrapper);
    }

    @Override
    public void updateTitle(String sessionId, String title) {
        LambdaUpdateWrapper<AiragSession> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AiragSession::getId, sessionId)
                .set(AiragSession::getTitle, title)
                .set(AiragSession::getUpdateTime, new Date());
        this.update(wrapper);
    }

    @Override
    public void deleteSession(String sessionId) {
        LambdaUpdateWrapper<AiragSession> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AiragSession::getId, sessionId)
                .set(AiragSession::getDelFlag, 1)
                .set(AiragSession::getUpdateTime, new Date());
        this.update(wrapper);
        
        // 同时删除消息
        messageMapper.delete(new LambdaQueryWrapper<AiragMessage>()
                .eq(AiragMessage::getSessionId, sessionId));
        
        log.info("删除会话: sessionId={}", sessionId);
    }

    @Override
    public void archiveSession(String sessionId) {
        LambdaUpdateWrapper<AiragSession> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AiragSession::getId, sessionId)
                .set(AiragSession::getStatus, 2)
                .set(AiragSession::getUpdateTime, new Date());
        this.update(wrapper);
        log.info("归档会话: sessionId={}", sessionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiragMessage addMessage(String sessionId, AiragMessage message) {
        message.setSessionId(sessionId);
        message.setCreateTime(new Date());
        messageMapper.insert(message);
        
        // 异步更新会话统计
        updateSessionStatsAsync(sessionId);
        
        return message;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addMessages(List<AiragMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }
        
        for (AiragMessage message : messages) {
            message.setCreateTime(new Date());
            messageMapper.insert(message);
        }
        
        // 更新第一个消息所属会话的统计
        String sessionId = messages.get(0).getSessionId();
        if (StrUtil.isNotBlank(sessionId)) {
            updateSessionStatsAsync(sessionId);
        }
    }

    @Override
    public List<AiragMessage> getSessionMessages(String sessionId) {
        return messageMapper.selectBySessionId(sessionId);
    }

    @Override
    public List<AiragMessage> getRecentMessages(String sessionId, int limit) {
        return messageMapper.selectRecentMessages(sessionId, limit);
    }

    @Override
    public void clearMessages(String sessionId) {
        messageMapper.delete(new LambdaQueryWrapper<AiragMessage>()
                .eq(AiragMessage::getSessionId, sessionId));
        
        // 重置会话统计
        LambdaUpdateWrapper<AiragSession> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AiragSession::getId, sessionId)
                .set(AiragSession::getMsgCount, 0)
                .set(AiragSession::getTotalTokens, 0)
                .set(AiragSession::getLastMessage, null)
                .set(AiragSession::getUpdateTime, new Date());
        this.update(wrapper);
        
        log.info("清空会话消息: sessionId={}", sessionId);
    }

    @Override
    public void updateSessionStats(String sessionId) {
        // 统计消息数量
        int msgCount = messageMapper.countBySessionId(sessionId);
        // 统计Token数
        int totalTokens = messageMapper.sumTokensBySessionId(sessionId);
        // 获取最后一条消息
        List<AiragMessage> messages = messageMapper.selectRecentMessages(sessionId, 1);
        String lastMessage = messages.isEmpty() ? null : messages.get(0).getContent();
        
        // 截取摘要
        String summary = null;
        if (StrUtil.isNotBlank(lastMessage) && lastMessage.length() > 100) {
            summary = lastMessage.substring(0, 100) + "...";
        } else {
            summary = lastMessage;
        }
        
        // 更新会话
        LambdaUpdateWrapper<AiragSession> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AiragSession::getId, sessionId)
                .set(AiragSession::getMsgCount, msgCount)
                .set(AiragSession::getTotalTokens, totalTokens)
                .set(AiragSession::getLastMessage, summary)
                .set(AiragSession::getLastActiveTime, new Date())
                .set(AiragSession::getUpdateTime, new Date());
        this.update(wrapper);
    }

    /**
     * 异步更新会话统计
     */
    @Async
    protected void updateSessionStatsAsync(String sessionId) {
        try {
            updateSessionStats(sessionId);
        } catch (Exception e) {
            log.error("更新会话统计失败: sessionId={}", sessionId, e);
        }
    }
}
