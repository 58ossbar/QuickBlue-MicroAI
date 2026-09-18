package com.budaos.ai.api.feign;

import com.budaos.ai.api.dto.AiAppDTO;
import com.budaos.common.core.domain.ApiResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * AI 应用远程服务接口
 */
@FeignClient(name = "QuickBlue-ai", contextId = "aiAppApi")
public interface AiAppApi {

    /**
     * 根据ID获取AI应用
     */
    @GetMapping("/ai/app/{id}")
    ApiResult<AiAppDTO> getById(@PathVariable("id") String id);

    /**
     * 检查应用是否存在
     */
    @GetMapping("/ai/app/exists/{id}")
    ApiResult<Boolean> existsById(@PathVariable("id") String id);
}
