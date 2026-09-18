package com.budaos.system.controller;

import com.budaos.api.system.dto.AuditLogDTO;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.system.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 内部接口控制器
 * 供微服务间调用，不对外暴露
 *
 * @author budaos
 */
@Slf4j
@RestController
@RequestMapping("/inner")
@RequiredArgsConstructor
public class InnerController {

    private final AuditLogService operateLogService;

    /**
     * 保存操作日志（内部接口，供其他微服务调用）
     */
    @PostMapping("/operateLog/save")
    public ApiResult<Void> saveOperateLog(@RequestBody AuditLogDTO operateLogDTO) {
        try {
            log.debug("收到内部日志保存请求: {}", operateLogDTO.getUrl());
            operateLogService.save(operateLogDTO);
            log.debug("操作日志保存成功: {}", operateLogDTO.getUrl());
            return ApiResult.ok();
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
            return ApiResult.paramError("保存操作日志失败");
        }
    }
}
