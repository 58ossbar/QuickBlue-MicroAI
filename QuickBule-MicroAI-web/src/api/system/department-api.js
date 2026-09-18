/*
 * 部门
 *

 */
import { getRequest, postRequest } from '/@/lib/axios';

export const departmentApi = {
    /**
     * 查询部门列表
     */
    queryAllDepartment: () => {
        return getRequest('/system/department/listAll');
    },

    /**
     * 查询部门树形列表
     */
    queryDepartmentTree: () => {
        return getRequest('/system/department/treeList');
    },

    /**
     * 添加部门
     */
    addDepartment: (param) => {
        return postRequest('/system/department/add', param);
    },
    /**
     * 更新部门信息
     */
    updateDepartment: (param) => {
        return postRequest('/system/department/update', param);
    },
    /**
     * 删除
     */
    deleteDepartment: (departmentId) => {
        return getRequest(`/system/department/delete/${departmentId}`);
    },
};