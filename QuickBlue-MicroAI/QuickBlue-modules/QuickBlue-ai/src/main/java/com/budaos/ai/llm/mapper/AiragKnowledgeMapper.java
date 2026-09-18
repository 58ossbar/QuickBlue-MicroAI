package com.budaos.ai.llm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.budaos.ai.llm.entity.AiragKnowledge;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI知识库 Mapper
 */
@Mapper
public interface AiragKnowledgeMapper extends BaseMapper<AiragKnowledge> {
}
