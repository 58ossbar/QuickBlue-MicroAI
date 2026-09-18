/*
 * AI模型管理 API
 *
 */
import { postRequest, getRequest, deleteRequest } from '/@/lib/axios';

export const aiModelApi = {
    // 获取激活的模型列表
    getList: () => {
        return getRequest('/ai/model/list');
    },

    // 分页查询模型
    getPage: (param) => {
        return getRequest('/ai/model/page', param);
    },

    // 根据ID获取模型
    getById: (id) => {
        return getRequest(`/ai/model/${id}`);
    },

    // 获取默认模型
    getDefaultModel: () => {
        return getRequest('/ai/model/default');
    },

    // 创建模型
    create: (param) => {
        return postRequest('/ai/model', param);
    },

    // 更新模型
    update: (param) => {
        return postRequest('/ai/model', param);
    },

    // 删除模型
    delete: (id) => {
        return deleteRequest(`/ai/model/${id}`);
    },

    // 批量删除模型
    batchDelete: (ids) => {
        return deleteRequest('/ai/model/batch', ids);
    },

    // 激活模型
    activate: (id) => {
        return postRequest(`/ai/model/activate/${id}`);
    },
};
