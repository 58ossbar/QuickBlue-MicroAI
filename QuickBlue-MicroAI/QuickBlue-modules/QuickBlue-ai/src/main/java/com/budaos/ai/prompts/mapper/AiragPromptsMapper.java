package com.budaos.ai.prompts.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.budaos.ai.prompts.entity.AiragPrompts;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI提示词 Mapper
 */
@Mapper
public interface AiragPromptsMapper extends BaseMapper<AiragPrompts> {
}
