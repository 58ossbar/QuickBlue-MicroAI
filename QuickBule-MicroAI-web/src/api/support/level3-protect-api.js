/**
 * 三级等保 api 封装
 *

 */
import { postRequest, getRequest } from '/@/lib/axios';

export const level3ProtectApi = {
    /**
     * 查询 三级等保配置
     */
    getConfig: () => {
        return getRequest('/support/protect/level3protect/getConfig');
    },

    /**
     * 更新三级等保配置
     */
    updateConfig: (form) => {
        return postRequest('/support/protect/level3protect/updateConfig', form);
    },

    /**
     * 是否开启密码复杂度校验
     */
    isPasswordComplexityEnabled: () => {
        return getRequest('/support/protect/level3protect/isPasswordComplexityEnabled');
    },
};
