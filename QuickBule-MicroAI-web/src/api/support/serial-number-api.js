/*
 * 单据序列号
 *

 */
import { postRequest, getRequest } from '/@/lib/axios';

export const serialNumberApi = {
    // 生成单号
    generate: (generateForm) => {
        return postRequest('/support/serialNumber/generate', generateForm);
    },
    // 获取所有单号定义
    getAll: () => {
        return getRequest('/support/serialNumber/all');
    },
    // 获取生成记录
    queryRecord: (form) => {
        return postRequest('/support/serialNumber/queryRecord', form);
    },
};