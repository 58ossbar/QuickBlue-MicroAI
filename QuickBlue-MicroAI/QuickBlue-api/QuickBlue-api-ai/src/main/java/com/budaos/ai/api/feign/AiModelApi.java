package com.budaos.ai.api.feign;

import com.budaos.ai.api.dto.AiModelDTO;
import com.budaos.common.core.domain.ApiResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * AI 模型远程服务接口
 */
@FeignClient(name = "QuickBlue-ai", contextId = "aiModelApi")
public interface AiModelApi {

    /**
     * 根据ID获取AI模型配置
     */
    @GetMapping("/ai/model/{id}")
    ApiResult<AiModelDTO> getById(@PathVariable("id") String id);

    /**
     * 获取默认模型配置
     */
    @GetMapping("/ai/model/default")
    ApiResult<AiModelDTO> getDefaultModel();
}
