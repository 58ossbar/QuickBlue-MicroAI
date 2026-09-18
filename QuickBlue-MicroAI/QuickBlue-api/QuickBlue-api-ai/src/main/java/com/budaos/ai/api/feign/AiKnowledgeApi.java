package com.budaos.ai.api.feign;

import com.budaos.ai.api.dto.AiKnowledgeDTO;
import com.budaos.common.core.domain.ApiResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * AI 知识库远程服务接口
 */
@FeignClient(name = "QuickBlue-ai", contextId = "aiKnowledgeApi")
public interface AiKnowledgeApi {

    /**
     * 根据ID获取知识库
     */
    @GetMapping("/ai/knowledge/{id}")
    ApiResult<AiKnowledgeDTO> getById(@PathVariable("id") String id);

    /**
     * 检查知识库是否存在
     */
    @GetMapping("/ai/knowledge/exists/{id}")
    ApiResult<Boolean> existsById(@PathVariable("id") String id);
}
