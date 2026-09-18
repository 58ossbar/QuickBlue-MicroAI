/*
 * AI知识库管理 API
 *
 */
import { postRequest, getRequest, deleteRequest, putRequest } from '/@/lib/axios';

export const aiKnowledgeApi = {
    // 获取知识库列表
    getList: () => {
        return getRequest('/ai/knowledge/list');
    },

    // 分页查询知识库
    getPage: (param) => {
        return getRequest('/ai/knowledge/page', param);
    },

    // 根据ID获取知识库
    getById: (id) => {
        return getRequest(`/ai/knowledge/${id}`);
    },

    // 创建知识库
    create: (param) => {
        return postRequest('/ai/knowledge', param);
    },

    // 更新知识库
    update: (param) => {
        return putRequest('/ai/knowledge', param);
    },

    // 删除知识库
    delete: (id) => {
        return deleteRequest(`/ai/knowledge/${id}`);
    },

    // 批量删除知识库
    batchDelete: (ids) => {
        return deleteRequest('/ai/knowledge/batch', ids);
    },

    // 检查知识库是否存在
    exists: (id) => {
        return getRequest(`/ai/knowledge/exists/${id}`);
    },

    // 启用知识库
    enable: (id) => {
        return putRequest(`/ai/knowledge/enable/${id}`);
    },

    // 禁用知识库
    disable: (id) => {
        return putRequest(`/ai/knowledge/disable/${id}`);
    },

    // 根据类型获取知识库
    getByType: (type) => {
        return getRequest(`/ai/knowledge/type/${type}`);
    },
};
