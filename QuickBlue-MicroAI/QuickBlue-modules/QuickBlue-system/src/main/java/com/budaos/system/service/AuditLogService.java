package com.budaos.system.service;

import com.budaos.api.system.dto.AuditLogDTO;
import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.system.domain.entity.AuditLogEntity;
import com.budaos.system.domain.form.AuditLogQueryForm;
import com.budaos.system.domain.vo.AuditLogVO;

import java.util.List;

/**
 * 操作日志服务接口
 *
 * @author budaos
 */
public interface AuditLogService {

    /**
     * 分页查询操作日志
     *
     * @param queryForm 查询表单
     * @return 分页结果
     */
    ApiResult<PageResponse<AuditLogVO>> queryByPage(AuditLogQueryForm queryForm);

    /**
     * 查询操作日志详情
     *
     * @param operateLogId 操作日志ID
     * @return 操作日志VO
     */
    ApiResult<AuditLogVO> detail(Long operateLogId);

    /**
     * 批量删除操作日志
     *
     * @param idList 日志ID列表
     * @return 操作结果
     */
    ApiResult<String> batchDelete(List<Long> idList);

    /**
     * 分页查询当前登录人信息
     *
     * @param queryForm 查询表单
     * @return 分页结果
     */
    ApiResult<PageResponse<AuditLogVO>> queryByPageLogin(AuditLogQueryForm queryForm);

    /**
     * 保存操作日志
     *
     * @param operateLogEntity 操作日志实体
     */
    void save(AuditLogEntity operateLogEntity);

    /**
     * 保存操作日志（从 DTO）
     *
     * @param operateLogDTO 操作日志 DTO
     */
    void save(AuditLogDTO operateLogDTO);
}
