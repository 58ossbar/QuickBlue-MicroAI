/*
 * 环境管理 API
 */

import { getRequest, postRequest, deleteRequest } from '/@/lib/axios';

export default {
  // 保存安装配置为环境
  saveEnvironment: (data) => {
    return postRequest('/environment/save', data);
  },

  // 获取所有环境配置
  listEnvironments: () => {
    return getRequest('/environment/list');
  },

  // 获取当前激活的环境
  getActiveEnvironment: () => {
    return getRequest('/environment/active');
  },

  // 切换环境
  switchEnvironment: (data) => {
    return postRequest('/environment/switch', data);
  },

  // 删除环境配置
  deleteEnvironment: (envName) => {
    return deleteRequest(`/environment/${envName}`);
  },

  // 测试环境数据库连接
  testEnvironmentDb: (envName) => {
    return getRequest(`/environment/${envName}/test-db`);
  },

  // 导出环境配置
  exportEnvironment: (envName) => {
    return getRequest(`/environment/${envName}/export`);
  },

  // 导入环境配置
  importEnvironment: (data) => {
    return postRequest('/environment/import', data);
  },

  // 更新环境的数据库连接信息
  updateEnvironmentDb: (envName, data) => {
    return postRequest(`/environment/${envName}/update-db`, data);
  }
};
