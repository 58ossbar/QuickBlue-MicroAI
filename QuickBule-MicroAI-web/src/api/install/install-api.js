/*
 * 安装向导 API
 */

import { getRequest, postRequest, getDownload } from '/@/lib/axios';

export default {
  // 环境检查
  checkEnvironment: () => {
    return getRequest('/install/check-environment');
  },

  // 测试数据库连接
  testDbConnection: (data) => {
    return postRequest('/install/test-db-connection', data);
  },

  // 测试Redis连接
  testRedisConnection: (data) => {
    return postRequest('/install/test-redis-connection', data);
  },

  // 测试Nacos连接
  testNacosConnection: (data) => {
    return postRequest('/install/test-nacos-connection', data);
  },

  // 测试云存储连接
  testCloudStorage: (data) => {
    return postRequest('/install/test-cloud-storage', data);
  },

  // 检查端口占用
  checkPort: (port) => {
    return getRequest('/install/check-port', { port });
  },

  // 开始安装
  startInstall: (data) => {
    return postRequest('/install/start', data);
  },

  // 获取安装进度
  getInstallProgress: () => {
    return getRequest('/install/progress');
  },

  // 下载安装配置
  downloadConfig: () => {
    return getDownload('/install/download-config');
  },

  // 检查系统是否已安装
  checkInstalled: () => {
    return getRequest('/install/check-installed');
  },

  // 获取系统信息
  getSystemInfo: () => {
    return getRequest('/install/system-info');
  }
};
