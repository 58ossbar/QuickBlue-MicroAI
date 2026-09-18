/*
 *  登录
 *

 */
import { getRequest, postRequest } from '/@/lib/axios';

export const loginApi = {
    /**
     * 登录
     */
    login: (param) => {
        return postRequest('/login', param);
    },

    /**
     * 退出登录
     */
    logout: () => {
        return getRequest('/login/logout');
    },

    /**
     * 获取验证码
     */
    getCaptcha: () => {
        return getRequest('/login/getCaptcha');
    },

    /**
     * 获取登录信息
     */
    getLoginInfo: () => {
        return getRequest('/login/getLoginInfo');
    },

    /**
     * 获取邮箱登录验证码
     */
    sendLoginEmailCode: (loginName) => {
        return getRequest(`/login/sendEmailCode/${loginName}`);
    },

    /**
     * 获取双因子登录标识
     */
    getTwoFactorLoginFlag: () => {
        return getRequest('/login/getTwoFactorLoginFlag');
    },
};