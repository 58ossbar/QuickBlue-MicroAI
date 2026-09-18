/**
 * 岗位表 api 封装
 *
 *
 */
import { postRequest, getRequest } from '/@/lib/axios';

export const positionApi = {

    /**
     * 分页查询  @author  kaiyun
     */
    queryPage: (param) => {
        return postRequest('/system/position/queryPage', param);
    },

    /**
     * 增加  @author  kaiyun
     */
    add: (param) => {
        return postRequest('/system/position/add', param);
    },

    /**
     * 修改  @author  kaiyun
     */
    update: (param) => {
        return postRequest('/system/position/update', param);
    },


    /**
     * 删除  @author  kaiyun
     */
    delete: (id) => {
        return getRequest(`/system/position/delete/${id}`);
    },

    /**
     * 批量删除  @author  kaiyun
     */
    batchDelete: (idList) => {
        return postRequest('/system/position/batchDelete', idList);
    },

    /**
     * 查询列表  @author  kaiyun
     */
    queryList: () => {
        return getRequest('/system/position/queryList');
    },

    /**
     * 查询岗位树  @author  kaiyun
     */
    queryTree: () => {
        return getRequest('/system/position/queryTree');
    },

};
