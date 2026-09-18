/*
 * 字典
 *

 */
import { postRequest, getRequest } from '/@/lib/axios';

export const dictApi = {

    // 获取所有字典code 
    getAllDict: () => {
        return getRequest('/support/dict/getAllDict');
    },

    // 获取全部字典数据 
    getAllDictData: () => {
        return getRequest('/support/dict/getAllDictData');
    },

    // 分页查询 
    queryDict: (param) => {
        return postRequest('/support/dict/queryPage', param);
    },

    // 添加 
    addDict: (param) => {
        return postRequest('/support/dict/add', param);
    },

    // 更新 
    updateDict: (param) => {
        return postRequest('/support/dict/update', param);
    },

    // 字典-删除-
    batchDeleteDict: (dictIdList) => {
        return postRequest('/support/dict/batchDelete', dictIdList);
    },

    // 字典 启用 禁用 
    updateDisabled: (dictId) => {
        return getRequest(`/support/dict/updateDisabled/${dictId}`);
    },

    // ------------- 查询字典数据 -------------

    // 字典数据 分页查询 
    queryDictData: (dictId) => {
        return getRequest(`/support/dict/dictData/queryDictData/${dictId}`);
    },

    // 字典数据 添加 -
    addDictData: (param) => {
        return postRequest('/support/dict/dictData/add', param);
    },

    // 字典数据 更新-
    updateDictData: (param) => {
        return postRequest('/support/dict/dictData/update', param);
    },

    // 字典数据-删除-
    batchDeleteDictData: (dictDataIdList) => {
        return postRequest('/support/dict/dictData/batchDelete', dictDataIdList);
    },

    // 字典数据 启用 禁用 
    updateDictDataDisabled: (dictDataId) => {
        return getRequest(`/support/dict/dictData/updateDisabled/${dictDataId}`);
    },
};
