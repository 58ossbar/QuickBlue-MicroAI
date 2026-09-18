package com.budaos.ai.session.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.budaos.ai.session.entity.AiragMessage;
import com.budaos.ai.session.entity.AiragSession;

import java.util.List;

/**
 * AI会话服务接口
 */
public interface ISessionService extends IService<AiragSession> {

    /**
     * 创建新会话
     *
     * @param appId  应用ID
     * @param userId 用户ID
     * @param title  会话标题
     * @return 会话实体
     */
    AiragSession createSession(String appId, String userId, String title);

    /**
     * 获取用户会话列表
     *
     * @param userId   用户ID
     * @param appId    应用ID(可选)
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 会话分页
     */
    Page<AiragSession> getUserSessions(String userId, String appId, int pageNum, int pageSize);

    /**
     * 获取会话详情
     *
     * @param sessionId 会话ID
     * @return 会话实体
     */
    AiragSession getSession(String sessionId);

    /**
     * 更新会话活跃时间
     *
     * @param sessionId 会话ID
     */
    void updateActiveTime(String sessionId);

    /**
     * 更新会话标题
     *
     * @param sessionId 会话ID
     * @param title     标题
     */
    void updateTitle(String sessionId, String title);

    /**
     * 删除会话(逻辑删除)
     *
     * @param sessionId 会话ID
     */
    void deleteSession(String sessionId);

    /**
     * 归档会话
     *
     * @param sessionId 会话ID
     */
    void archiveSession(String sessionId);

    /**
     * 添加消息到会话
     *
     * @param sessionId 会话ID
     * @param message   消息实体
     * @return 保存后的消息
     */
    AiragMessage addMessage(String sessionId, AiragMessage message);

    /**
     * 批量添加消息
     *
     * @param messages 消息列表
     */
    void addMessages(List<AiragMessage> messages);

    /**
     * 获取会话消息列表
     *
     * @param sessionId 会话ID
     * @return 消息列表
     */
    List<AiragMessage> getSessionMessages(String sessionId);

    /**
     * 获取会话最近N条消息(用于上下文)
     *
     * @param sessionId 会话ID
     * @param limit     数量限制
     * @return 消息列表
     */
    List<AiragMessage> getRecentMessages(String sessionId, int limit);

    /**
     * 清空会话消息
     *
     * @param sessionId 会话ID
     */
    void clearMessages(String sessionId);

    /**
     * 更新会话统计信息
     *
     * @param sessionId 会话ID
     */
    void updateSessionStats(String sessionId);
}
