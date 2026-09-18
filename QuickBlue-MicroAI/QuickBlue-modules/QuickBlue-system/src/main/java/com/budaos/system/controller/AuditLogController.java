package com.budaos.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.budaos.api.system.dto.AuditLogDTO;
import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.system.domain.entity.AuditLogEntity;
import com.budaos.system.domain.form.AuditLogQueryForm;
import com.budaos.system.domain.vo.AuditLogVO;
import com.budaos.system.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 操作日志控制器
 *
 * @author budaos
 */
@Tag(name = "操作日志管理", description = "操作日志管理相关接口")
@RestController
@RequestMapping("/operateLog")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService operateLogService;

    /**
     * 分页查询操作日志
     */
    @Operation(summary = "分页查询操作日志")
    @PostMapping("/page/query")
    @SaCheckPermission("support:operateLog:query")
    public ApiResult<PageResponse<AuditLogVO>> queryOperateLogByPage(@RequestBody AuditLogQueryForm queryForm) {
        return operateLogService.queryByPage(queryForm);
    }

    /**
     * 查询操作日志详情
     */
    @Operation(summary = "查询操作日志详情")
    @GetMapping("/detail/{operateLogId}")
    @SaCheckPermission("support:operateLog:detail")
    public ApiResult<AuditLogVO> detailOperateLog(@PathVariable Long operateLogId) {
        return operateLogService.detail(operateLogId);
    }

    /**
     * 批量删除操作日志
     */
    @Operation(summary = "批量删除操作日志")
    @PostMapping("/batchDelete")
    @SaCheckPermission("support:operateLog:delete")
    public ApiResult<String> batchDeleteOperateLog(@RequestBody List<Long> idList) {
        return operateLogService.batchDelete(idList);
    }

    /**
     * 分页查询当前登录人操作日志
     */
    @Operation(summary = "分页查询当前登录人操作日志")
    @PostMapping("/page/query/login")
    public ApiResult<PageResponse<AuditLogVO>> queryByPageLogin(@RequestBody AuditLogQueryForm queryForm) {
        return operateLogService.queryByPageLogin(queryForm);
    }
}
