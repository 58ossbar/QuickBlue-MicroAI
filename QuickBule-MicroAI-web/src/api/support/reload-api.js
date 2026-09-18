/*
 * reload (内存热加载、钩子等)
 *

 */
import { postRequest, getRequest } from '/@/lib/axios';

export const reloadApi = {
    // 查询reload列表
    queryList: () => {
        return getRequest('/support/reload/query');
    },
    // 获取reload result
    queryReloadResult: (tag) => {
        return getRequest(`/support/reload/result/${tag}`);
    },
    // 执行reload
    reload: (reloadForm) => {
        return postRequest('/support/reload/update', reloadForm);
    },
};