/*
 * 数据权限配置 API
 *
 */
import { postRequest, getRequest } from '/@/lib/axios';

export const dataScopeConfigApi = {
    // 获取列表（启用状态）
    list: () => {
        return getRequest('/system/data-scope-config/list');
    },
    // 分页查询
    queryPage: (param) => {
        return postRequest('/system/data-scope-config/queryPage', param);
    },
    // 添加
    add: (param) => {
        return postRequest('/system/data-scope-config/add', param);
    },
    // 更新
    update: (param) => {
        return postRequest('/system/data-scope-config/update', param);
    },
    // 删除
    delete: (configId) => {
        return getRequest(`/system/data-scope-config/delete/${configId}`);
    },
    // 批量删除
    batchDelete: (idList) => {
        return postRequest('/system/data-scope-config/batchDelete', idList);
    },
    // 更新状态（启用/禁用）
    updateStatus: (configId) => {
        return getRequest(`/system/data-scope-config/updateStatus/${configId}`);
    },
    // 获取视图类型列表
    getViewTypeList: () => {
        return getRequest('/system/data-scope-config/view-type-list');
    },
};
