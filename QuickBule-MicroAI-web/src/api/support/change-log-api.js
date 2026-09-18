/**
 * 系统更新日志 api 封装
 *

 */
import { postRequest, getRequest } from '/@/lib/axios';

export const changeLogApi = {
    /**
     * 分页查询  
     */
    queryPage: (param) => {
        return postRequest('/support/changeLog/queryPage', param);
    },

    /**
     * 增加  
     */
    add: (param) => {
        return postRequest('/support/changeLog/add', param);
    },

    /**
     * 修改  
     */
    update: (param) => {
        return postRequest('/support/changeLog/update', param);
    },

    /**
     * 删除  
     */
    delete: (id) => {
        return getRequest(`/support/changeLog/delete/${id}`);
    },

    /**
     * 批量删除  
     */
    batchDelete: (idList) => {
        return postRequest('/support/changeLog/batchDelete', idList);
    },
};
