/**
 * 登录锁定 api 封装
 *

 */
import { postRequest, getRequest } from '/@/lib/axios';

export const loginFailApi = {

    /**
     * 分页查询
     */
    queryPage: (param) => {
        return postRequest('/system/protect/loginFail/queryPage', param);
    },

    /**
     * 批量删除
     */
    batchDelete: (idList) => {
        return postRequest('/system/protect/loginFail/batchDelete', idList);
    },

};
