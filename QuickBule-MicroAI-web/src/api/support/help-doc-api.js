/*
 * 帮助文档
 *
 */
import { postRequest, getRequest } from '/@/lib/axios';

export const helpDocApi = {
    // 【管理】帮助文档-分页查询
    query: (param) => {
        return postRequest('/support/helpDoc/query', param);
    },

    //【管理】帮助文档-更新
    update: (param) => {
        return postRequest('/support/helpDoc/update', param);
    },

    // 【管理】帮助文档-添加
    add: (param) => {
        return postRequest('/support/helpDoc/add', param);
    },

    //【管理】帮助文档-删除
    delete: (helpDocId) => {
        return getRequest(`/support/helpDoc/delete/${helpDocId}`);
    },

    //【管理】帮助文档-获取详情
    getDetail: (helpDocId) => {
        return getRequest(`/support/helpDoc/getDetail/${helpDocId}`);
    },

    //【管理】帮助文档-根据关联id查询
    queryHelpDocByRelationId: (relationId) => {
        return getRequest(`/support/helpDoc/queryHelpDocByRelationId/${relationId}`);
    },

    //----------------------- 用户相关 --------------------------------

    //【用户】帮助文档-查询全部
    getAllHelpDocList() {
        return getRequest('/support/helpDoc/user/queryAllHelpDocList');
    },

    //【用户】帮助文档-查询全部
    view(helpDocId) {
        return getRequest(`/support/helpDoc/user/view/${helpDocId}`);
    },

    //【用户】帮助文档-查询 查看记录
    queryViewRecord(param) {
        return postRequest('/support/helpDoc/user/queryViewRecord', param);
    },

    //【用户】帮助文档-获取完整数据（用于导出整个PDF）
    getComplete() {
        return getRequest('/support/helpDoc/user/getComplete');
    },
};
