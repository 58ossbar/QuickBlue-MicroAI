/*
 * 操作日志
 *

 */
import { postRequest, getRequest } from '/@/lib/axios';

export const operateLogApi = {
    // 分页查询
    queryList: (param) => {
        return postRequest('/system/operateLog/page/query', param);
    },
    // 详情
    detail: (id) => {
        return getRequest(`/system/operateLog/detail/${id}`);
    },
    // 分页查询当前登录人信息 @author 善逸
    queryListLogin: (param) => {
        return postRequest('/system/operateLog/page/query/login', param);
    },
};