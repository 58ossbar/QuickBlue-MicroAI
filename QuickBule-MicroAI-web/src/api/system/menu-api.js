/*
 * 菜单
 *

 */
import { getRequest, postRequest } from '/@/lib/axios';

export const menuApi = {
    /**
     * 添加菜单
     */
    addMenu: (param) => {
        return postRequest('/system/menu/add', param);
    },

    /**
     * 更新菜单
     */
    updateMenu: (param) => {
        return postRequest('/system/menu/update', param);
    },

    /**
     * 批量删除菜单
     */
    batchDeleteMenu: (menuIdList) => {
        return getRequest(`/system/menu/batchDelete?menuIdList=${menuIdList}`);
    },

    /**
     * 查询所有菜单列表
     */
    queryMenu: () => {
        return getRequest('/system/menu/query');
    },

    /**
     * 查询菜单树
     */
    queryMenuTree: (onlyMenu) => {
        return getRequest(`/system/menu/tree?onlyMenu=${onlyMenu}`);
    },

    /**
     * 获取所有请求路径
     */
    getAuthUrl: () => {
        return getRequest('/system/menu/auth/url');
    },
};