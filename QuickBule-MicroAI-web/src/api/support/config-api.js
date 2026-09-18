/*
 * 配置
 *

 */
import { postRequest, getRequest } from '/@/lib/axios';

export const configApi = {
    // 分页查询
    queryList: (param) => {
        return postRequest('/support/config/query', param);
    },
    // 添加配置参数
    addConfig: (param) => {
        return postRequest('/support/config/add', param);
    },
    // 修改配置参数
    updateConfig: (param) => {
        return postRequest('/support/config/update', param);
    },
    // 查询配置详情
    queryByKey: (param) => {
        return getRequest(`/support/config/queryByKey?configKey=${param}`);
    },
};