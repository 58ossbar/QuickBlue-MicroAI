/*
 * Nacos配置管理 API
 *
 */
import { postRequest, getRequest, deleteRequest } from '/src/lib/axios';

export const nacosConfigApi = {
    // 获取命名空间列表
    listNamespaces: () => {
        return getRequest('/support/nacos-config/namespaces');
    },
    // 获取配置列表
    listConfigs: (tenantId, groupId, pageNo, pageSize) => {
        return getRequest(`/support/nacos-config/configs?tenantId=${tenantId || ''}&groupId=${groupId || ''}&pageNo=${pageNo || 1}&pageSize=${pageSize || 10}`);
    },
    // 获取配置详情
    getConfig: (dataId, groupId, tenantId) => {
        return getRequest(`/support/nacos-config/config?dataId=${dataId}&groupId=${groupId}&tenantId=${tenantId || ''}`);
    },
    // 发布配置
    publishConfig: (param) => {
        return postRequest('/support/nacos-config/publish', param);
    },
    // 删除配置
    deleteConfig: (dataId, groupId, tenantId) => {
        return deleteRequest(`/support/nacos-config/config?dataId=${dataId}&groupId=${groupId}&tenantId=${tenantId || ''}`);
    },
    // 获取配置历史版本
    getConfigHistory: (dataId, groupId, tenantId, pageNo, pageSize) => {
        return getRequest(`/support/nacos-config/history?dataId=${dataId}&groupId=${groupId}&tenantId=${tenantId || ''}&pageNo=${pageNo || 1}&pageSize=${pageSize || 10}`);
    },
    // 获取配置历史版本详情
    getConfigHistoryDetail: (nid, dataId, groupId, tenantId) => {
        return getRequest(`/support/nacos-config/history/detail?nid=${nid}&dataId=${dataId}&groupId=${groupId}&tenantId=${tenantId || ''}`);
    },
    // 回滚配置
    rollbackConfig: (nid, dataId, groupId, tenantId) => {
        return postRequest(`/support/nacos-config/rollback?nid=${nid}&dataId=${dataId}&groupId=${groupId}&tenantId=${tenantId || ''}`);
    },
    // 分页查询配置审计记录
    queryAuditByPage: (param) => {
        return postRequest('/support/nacos-config/audit/page', param);
    },
    // 查询审计详情
    getAuditDetail: (auditId) => {
        return getRequest(`/support/nacos-config/audit/${auditId}`);
    },
    // 导出配置
    exportConfigs: (dataIds, tenantId, groupId) => {
        return postRequest(`/support/nacos-config/export?tenantId=${tenantId || ''}&groupId=${groupId || ''}`, dataIds || []);
    },
    // 导入配置
    importConfigs: (configs) => {
        return postRequest('/support/nacos-config/import', configs);
    },
    // 从本地 nacos_config 目录一键同步配置到 Nacos
    syncFromLocal: (tenantId) => {
        return postRequest(`/support/nacos-config/sync-from-local?tenantId=${tenantId || ''}`);
    },
};

// ==================== 配置模板相关API ====================

export const nacosConfigTemplateApi = {
    // 分页查询配置模板
    queryTemplatePage: (param) => {
        return postRequest('/support/nacos-config-template/query', param);
    },
    // 查询所有模板列表
    listAllTemplates: () => {
        return getRequest('/support/nacos-config-template/list');
    },
    // 根据ID查询模板详情
    getTemplateById: (templateId) => {
        return getRequest(`/support/nacos-config-template/detail/${templateId}`);
    },
    // 根据模板编码查询模板详情
    getTemplateByCode: (templateCode) => {
        return getRequest(`/support/nacos-config-template/detail/code/${templateCode}`);
    },
    // 添加配置模板
    addTemplate: (param) => {
        return postRequest('/support/nacos-config-template/add', param);
    },
    // 更新配置模板
    updateTemplate: (param) => {
        return postRequest('/support/nacos-config-template/update', param);
    },
    // 删除配置模板
    deleteTemplate: (templateId) => {
        return deleteRequest(`/support/nacos-config-template/${templateId}`);
    },
};
