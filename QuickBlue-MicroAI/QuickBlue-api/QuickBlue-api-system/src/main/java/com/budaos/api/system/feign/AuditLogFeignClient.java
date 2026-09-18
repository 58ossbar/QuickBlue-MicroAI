package com.budaos.api.system.feign;

import com.budaos.api.system.dto.AuditLogDTO;
import com.budaos.common.core.domain.ApiResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 操作日志 Feign 客户端
 *
 * @author budaos
 */
@FeignClient(contextId = "operateLogFeignClient", value = "QuickBlue-system")
public interface AuditLogFeignClient {

    /**
     * 保存操作日志（内部接口，不需要权限验证）
     */
    @PostMapping("/inner/operateLog/save")
    ApiResult<Void> saveOperateLog(@RequestBody AuditLogDTO operateLogDTO);
}
