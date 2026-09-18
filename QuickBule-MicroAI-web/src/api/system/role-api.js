/*
 * 角色
 *

 */
import { getRequest, postRequest } from '/@/lib/axios';

export const roleApi = {
    /**
     * @description: 获取所有角色
     */
    queryAll: () => {
        return getRequest('/system/role/getAll');
    },
    /**
     * @description:添加角色
     */
    addRole: (data) => {
        return postRequest('/system/role/add', data);
    },
    /**
     * @description:更新角色
     */
    updateRole: (data) => {
        return postRequest('/system/role/update', data);
    },
    /**
     * @description: 删除角色
     */
    deleteRole: (roleId) => {
        return getRequest(`/system/role/delete/${roleId}`);
    },
    /**
     * @description: 批量设置某角色数据范围
     */
    updateDataScope: (data) => {
        return postRequest('/system/role/dataScope/updateRoleDataScopeList', data);
    },
    /**
     * @description: 获取当前系统所配置的所有数据范围
     */
    getDataScopeList: () => {
        return getRequest('/system/dataScope/list');
    },
    /**
     * @description: 获取某角色所设置的数据范围
     */
    getDataScopeByRoleId: (roleId) => {
        return getRequest(`/system/role/dataScope/getRoleDataScopeList/${roleId}`);
    },
    /**
     * @description: 获取角色成员-员工列表
     */
    queryRoleEmployee: (params) => {
        return postRequest('/system/role/employee/queryEmployee', params);
    },
    /**
     * @description: 从角色成员列表中移除员工
     */
    deleteEmployeeRole: (employeeId, roleId) => {
        return getRequest('/system/role/employee/removeEmployee?employeeId=' + employeeId + '&roleId=' + roleId);
    },
    /**
     * @description: 从角色成员列表中批量移除员工
     */
    batchRemoveRoleEmployee: (data) => {
        return postRequest('/system/role/employee/batchRemoveRoleEmployee', data);
    },
    /**
     * @description: 根据角色id获取角色员工列表(无分页)
     */
    getRoleAllEmployee: (roleId) => {
        return getRequest(`/system/role/employee/getAllEmployeeByRoleId/${roleId}`);
    },
    /**
     * @description: 角色成员列表中批量添加员工
     */
    batchAddRoleEmployee: (data) => {
        return postRequest('/system/role/employee/batchAddRoleEmployee', data);
    },
};