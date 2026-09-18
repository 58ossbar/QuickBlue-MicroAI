/**
 * 系统参数配置 api 封装
 *
 */
import { postRequest, getRequest } from '/@/lib/axios';

export const systemConfigApi = {
    /**
     * 查询系统参数配置
     */
    getConfig: () => {
        return getRequest('/systemconfig/config/get');
    },

    /**
     * 更新系统参数配置
     */
    updateConfig: (form) => {
        return postRequest('/systemconfig/config/update', form);
    },
};
