package com.budaos.ai.session.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.budaos.ai.session.entity.AiragMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AI消息Mapper
 */
@Mapper
public interface AiragMessageMapper extends BaseMapper<AiragMessage> {

    /**
     * 根据会话ID查询消息列表
     */
    List<AiragMessage> selectBySessionId(@Param("sessionId") String sessionId);

    /**
     * 根据会话ID查询最近N条消息
     */
    List<AiragMessage> selectRecentMessages(@Param("sessionId") String sessionId, @Param("limit") int limit);

    /**
     * 根据会话ID统计消息数量
     */
    int countBySessionId(@Param("sessionId") String sessionId);

    /**
     * 根据会话ID统计Token数
     */
    int sumTokensBySessionId(@Param("sessionId") String sessionId);
}
