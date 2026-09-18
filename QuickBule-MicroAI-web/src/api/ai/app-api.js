/*
 * AI应用管理 API
 *
 */
import { postRequest, getRequest, deleteRequest, putRequest } from '/@/lib/axios';

export const aiAppApi = {
    // 获取应用列表
    getList: () => {
        return getRequest('/ai/app/list');
    },

    // 分页查询应用
    getPage: (param) => {
        return getRequest('/ai/app/page', param);
    },

    // 根据ID获取应用
    getById: (id) => {
        return getRequest(`/ai/app/${id}`);
    },

    // 创建应用
    create: (param) => {
        return postRequest('/ai/app', param);
    },

    // 更新应用
    update: (param) => {
        return postRequest('/ai/app', param);
    },

    // 删除应用
    delete: (id) => {
        return deleteRequest(`/ai/app/${id}`);
    },

    // 批量删除应用
    batchDelete: (ids) => {
        return deleteRequest('/ai/app/batch', ids);
    },

    // 检查应用是否存在
    exists: (id) => {
        return getRequest(`/ai/app/exists/${id}`);
    },

    // 发布应用
    publish: (id) => {
        return putRequest(`/ai/app/publish/${id}`);
    },

    // 启用应用
    enable: (id) => {
        return putRequest(`/ai/app/enable/${id}`);
    },

    // 禁用应用
    disable: (id) => {
        return putRequest(`/ai/app/disable/${id}`);
    },
};
