package com.budaos.system.service;

import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.system.domain.entity.SysPermissionDataRule;

import java.util.List;

/**
 * 数据权限规则服务接口
 *
 * @author QuickBlue
 */
public interface SysPermissionDataRuleService {

    /**
     * 查询数据权限规则列表
     *
     * @param permissionId 权限ID
     * @return 规则列表
     */
    List<SysPermissionDataRule> queryRuleList(Long permissionId);

    /**
     * 分页查询数据权限规则
     *
     * @param permissionId 权限ID
     * @param pageNum     页码
     * @param pageSize    页大小
     * @return 分页结果
     */
    PageResponse<SysPermissionDataRule> queryPage(Long permissionId, Integer pageNum, Integer pageSize);

    /**
     * 根据ID查询数据权限规则
     *
     * @param ruleId 规则ID
     * @return 规则实体
     */
    SysPermissionDataRule selectById(Long ruleId);

    /**
     * 添加数据权限规则
     *
     * @param rule 规则实体
     * @return 操作结果
     */
    ApiResult<String> add(SysPermissionDataRule rule);

    /**
     * 更新数据权限规则
     *
     * @param rule 规则实体
     * @return 操作结果
     */
    ApiResult<String> update(SysPermissionDataRule rule);

    /**
     * 删除数据权限规则
     *
     * @param ruleId 规则ID
     * @return 操作结果
     */
    ApiResult<String> delete(Long ruleId);

    /**
     * 批量删除数据权限规则
     *
     * @param ruleIds 规则ID列表
     * @return 操作结果
     */
    ApiResult<String> batchDelete(List<Long> ruleIds);
}
