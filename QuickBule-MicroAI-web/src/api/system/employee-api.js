/*
 *  员工
 *

 */

import { getRequest, postEncryptRequest, postRequest, request } from '/@/lib/axios';

export const employeeApi = {
    /**
     * 查询所有员工
     */
    queryAll: () => {
        return getRequest('/system/employee/queryAll');
    },
    /**
     * 员工管理查询
     */
    queryEmployee: (params) => {
        return postRequest('/system/employee/query', params);
    },
    /**
     * 添加员工
     */
    addEmployee: (params) => {
        return postRequest('/system/employee/add', params);
    },
    /**
     * 更新员工信息
     */
    updateEmployee: (params) => {
        return postRequest('/system/employee/update', params);
    },
    /**
     * 更新员工个人中心信息
     */
    updateCenter: (params) => {
        return postRequest('/system/employee/update/center', params);
    },
    /**
     * 更新登录人头像
     */
    updateAvatar: (params) => {
        return postRequest('/system/employee/update/avatar', params);
    },
    /**
     * 删除员工
     */
    deleteEmployee: (employeeId) => {
        return getRequest(`/system/employee/delete/${employeeId}`);
    },
    /**
     * 批量删除员工
     */
    batchDeleteEmployee: (employeeIdList) => {
        return postRequest('/system/employee/update/batch/delete', employeeIdList);
    },
    /**
     * 批量调整员工部门
     */
    batchUpdateDepartmentEmployee: (updateParam) => {
        return postRequest('/system/employee/update/batch/department', updateParam);
    },
    /**
     * 重置员工密码
     */
    resetPassword: (employeeId) => {
        return getRequest(`/system/employee/update/password/reset/${employeeId}`);
    },
    /**
     * 修改密码
     */
    updateEmployeePassword: (param) => {
        return postEncryptRequest('/system/employee/update/password', param);
    },
    /**
     * 更新员工禁用状态
     */
    updateDisabled: (employeeId) => {
        return getRequest(`/system/employee/update/disabled/${employeeId}`);
    },

    /**
     * 查询员工-根据部门id
     */
    queryEmployeeByDeptId: (departmentId) => {
        return getRequest(`/system/employee/getAllEmployeeByDepartmentId/${departmentId}`);
    },

    /**
     * 导出员工数据
     */
    exportEmployee: (params) => {
        return request({
            url: '/system/employee/export',
            method: 'get',
            params: {
                disabledFlag: params.disabledFlag,
                departmentId: params.departmentId,
                includeSubDepartment: params.includeSubDepartment,
                keyword: params.keyword,
            },
            responseType: 'blob'
        });
    },

    /**
     * 下载员工导入模板
     */
    downloadTemplate: () => {
        return request({
            url: '/system/employee/template',
            method: 'get',
            responseType: 'blob'
        });
    },

    /**
     * 导入员工数据
     */
    importEmployee: (file) => {
        // 如果传入的是FormData,直接使用;否则创建新的FormData
        const formData = file instanceof FormData ? file : (() => {
            const fd = new FormData();
            fd.append('file', file);
            return fd;
        })();
        return postRequest('/system/employee/import', formData, {
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        });
    },
};
