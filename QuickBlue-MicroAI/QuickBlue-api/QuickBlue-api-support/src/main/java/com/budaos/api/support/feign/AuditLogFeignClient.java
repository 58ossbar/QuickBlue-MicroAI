package com.budaos.api.support.feign;

import com.budaos.api.support.dto.AuditLogDTO;
import com.budaos.common.core.domain.PageQuery;
import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.ApiResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 操作日志服务 Feign 接口
 *
 * @author budaos
 */
@FeignClient(
    contextId = "operateLogFeignClient",
    name = "QuickBlue-support",
    path = "/operatelog"
)
public interface AuditLogFeignClient {

    /**
     * 分页查询操作日志
     *
     * @param form 查询表单
     * @return 分页结果
     */
    @PostMapping("/query")
    ApiResult<PageResponse<AuditLogDTO>> queryPage(@RequestBody PageQuery form);

    /**
     * 保存操作日志
     *
     * @param module         操作模块
     * @param operateType    操作类型
     * @param description    操作说明
     * @param requestPath    请求路径
     * @param requestMethod  请求方法
     * @param requestParams  请求参数
     * @param responseResult 响应结果
     * @param executeTime    执行时长
     * @return 保存结果
     */
    @PostMapping("/save")
    ApiResult<Void> save(
            @RequestParam("module") String module,
            @RequestParam("operateType") String operateType,
            @RequestParam("description") String description,
            @RequestParam("requestPath") String requestPath,
            @RequestParam("requestMethod") String requestMethod,
            @RequestParam(value = "requestParams", required = false) String requestParams,
            @RequestParam(value = "responseResult", required = false) String responseResult,
            @RequestParam("executeTime") Long executeTime
    );
}
