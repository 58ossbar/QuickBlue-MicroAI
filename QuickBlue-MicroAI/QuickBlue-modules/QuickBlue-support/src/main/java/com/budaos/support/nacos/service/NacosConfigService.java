package com.budaos.support.nacos.service;

import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.support.nacos.domain.form.ConfigAuditQueryForm;
import com.budaos.support.nacos.domain.form.NacosConfigForm;
import com.budaos.support.nacos.domain.vo.ConfigAuditVO;
import com.budaos.support.nacos.domain.vo.ConfigHistoryVO;
import com.budaos.support.nacos.domain.vo.NacosConfigVO;
import com.budaos.support.nacos.domain.vo.NacosNamespaceVO;

import java.util.List;

/**
 * Nacos配置管理服务接口
 *
 * @author budaos
 * @since 2026-02-24
 */
public interface NacosConfigService {

    /**
     * 获取所有命名空间
     *
     * @return 命名空间列表
     */
    ApiResult<List<NacosNamespaceVO>> listNamespaces();

    /**
     * 获取配置列表(根据命名空间)
     *
     * @param tenantId 命名空间ID
     * @param groupId  配置分组
     * @param pageNo   页码
     * @param pageSize 每页大小
     * @return 配置列表分页结果
     */
    ApiResult<PageResponse<NacosConfigVO>> listConfigs(String tenantId, String groupId, Integer pageNo, Integer pageSize);

    /**
     * 获取配置详情
     *
     * @param dataId   配置ID
     * @param groupId  配置分组
     * @param tenantId 命名空间ID
     * @return 配置详情
     */
    ApiResult<NacosConfigVO> getConfig(String dataId, String groupId, String tenantId);

    /**
     * 发布配置
     *
     * @param form 配置表单
     * @return 操作结果
     */
    ApiResult<String> publishConfig(NacosConfigForm form);

    /**
     * 删除配置
     *
     * @param dataId   配置ID
     * @param groupId  配置分组
     * @param tenantId 命名空间ID
     * @return 操作结果
     */
    ApiResult<String> deleteConfig(String dataId, String groupId, String tenantId);

    /**
     * 获取配置历史版本
     *
     * @param dataId   配置ID
     * @param groupId  配置分组
     * @param tenantId 命名空间ID
     * @param pageNo   页码
     * @param pageSize 每页大小
     * @return 历史版本列表
     */
    ApiResult<PageResponse<ConfigHistoryVO>> getConfigHistory(String dataId, String groupId, String tenantId, Integer pageNo, Integer pageSize);

    /**
     * 获取配置历史版本详情
     *
     * @param nid      历史版本ID
     * @param dataId   配置ID
     * @param groupId  配置分组
     * @param tenantId 命名空间ID
     * @return 历史版本详情
     */
    ApiResult<ConfigHistoryVO> getConfigHistoryDetail(String nid, String dataId, String groupId, String tenantId);

    /**
     * 回滚配置到指定版本
     *
     * @param nid      历史版本ID
     * @param dataId   配置ID
     * @param groupId  配置分组
     * @param tenantId 命名空间ID
     * @return 操作结果
     */
    ApiResult<String> rollbackConfig(String nid, String dataId, String groupId, String tenantId);

    /**
     * 分页查询配置审计记录
     *
     * @param queryForm 查询条件
     * @return 审计记录分页结果
     */
    ApiResult<PageResponse<ConfigAuditVO>> queryAuditByPage(ConfigAuditQueryForm queryForm);

    /**
     * 查询审计详情
     *
     * @param auditId 审计ID
     * @return 审计详情
     */
    ApiResult<ConfigAuditVO> getAuditDetail(Long auditId);

    /**
     * 导出配置
     *
     * @param dataIds  配置ID列表（为空则导出全部）
     * @param tenantId 命名空间ID
     * @param groupId  配置分组
     * @return 导出内容
     */
    ApiResult<List<NacosConfigVO>> exportConfigs(List<String> dataIds, String tenantId, String groupId);

    /**
     * 导入配置
     *
     * @param configs 配置列表
     * @return 导入结果
     */
    ApiResult<String> importConfigs(List<NacosConfigForm> configs);
}
