/**
 * 区域管理 API
 */
import { postRequest, getRequest } from '/@/lib/axios';

export const regionApi = {
    /**
     * 查询区域树（带条件）
     */
    queryTree: (param) => {
        return postRequest('/business/region/queryTree', param);
    },

    /**
     * 查询区域树形列表（无查询条件）
     */
    treeList: () => {
        return getRequest('/business/region/treeList');
    },

    /**
     * 添加区域
     */
    add: (param) => {
        return postRequest('/business/region/add', param);
    },

    /**
     * 修改区域
     */
    update: (param) => {
        return postRequest('/business/region/update', param);
    },

    /**
     * 删除区域
     */
    delete: (id) => {
        return getRequest(`/business/region/delete/${id}`);
    },
};
