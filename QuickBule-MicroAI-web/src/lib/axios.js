/*
 *  ajax请求
 *
 */
import { message, Modal } from 'ant-design-vue';
import axios from 'axios';
import { localRead } from '/@/utils/local-util';
import { useUserStore } from '/@/store/modules/system/user';
import { decryptData, encryptData } from './encrypt';
import { DATA_TYPE_ENUM } from '../constants/common-const';
import _ from 'lodash';
import LocalStorageKeyConst from '../constants/local-storage-key-const.js';

// token的消息头
const TOKEN_HEADER = 'Authorization';

// 创建axios对象
const axiosInstance = axios.create({
  baseURL: import.meta.env.VITE_APP_API_URL,
  timeout: 30000, // 设置30秒超时
});

// 退出系统
function logout() {
  useUserStore().logout();
  location.href = '/';
}

// ================================= 错误码定义 =================================

/**
 * 系统错误码（与后端 SystemErrorCode 保持一致）
 */
const SystemErrorCode = {
  SYSTEM_ERROR: 50001,
  DB_ERROR: 50002,
  FILE_ERROR: 50004,
  CONFIG_ERROR: 50005,
  SERVICE_UNAVAILABLE: 50100,
  SERVICE_TIMEOUT: 50101,
  NETWORK_CONNECT_ERROR: 50102,
  GATEWAY_ERROR: 50103,
  SERVICE_DEGRADATION: 50104,
  TOKEN_INVALID: 50200,
  TOKEN_EXPIRED: 50201,
  PERMISSION_DENIED: 50202,
  BUSINESS_ERROR: 50300,
  DATA_VALIDATE_ERROR: 50301,
  CONCURRENT_ERROR: 50302,
};

/**
 * 用户错误码（与后端 UserErrorCode 保持一致）
 */
const UserErrorCode = {
  PARAM_ERROR: 30001,
  DATA_NOT_EXIST: 30002,
  DATA_DELETED: 30003,
  ALREADY_EXIST: 30004,
  REPEAT_SUBMIT: 30005,
  NO_PERMISSION: 30006,
  DEVELOPING: 30007,
  LOGIN_STATE_INVALID: 30008,
  USER_STATUS_ERROR: 30009,
  FORM_REPEAT_SUBMIT: 30010,
  LOGIN_FAIL_LOCK: 30011,
  LOGIN_FAIL_WILL_LOCK: 30012,
  LOGIN_ACTIVE_TIMEOUT: 30013,
};

// ================================= 错误处理工具 =================================

/**
 * 错误类型枚举
 */
const ErrorType = {
  NETWORK: 'network',           // 网络错误（无法连接到服务器）
  TIMEOUT: 'timeout',           // 请求超时
  SERVER_DOWN: 'server_down',   // 服务器宕机/未启动
  BUSINESS: 'business',         // 业务错误
  AUTH: 'auth',                 // 认证授权错误
  UNKNOWN: 'unknown',           // 未知错误
};

/**
 * 分析错误类型
 */
function analyzeError(error) {
  // 没有响应对象，说明请求根本没发出去或者服务器没响应
  if (!error.response) {
    if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
      return { type: ErrorType.TIMEOUT, message: '请求超时，请检查网络连接' };
    }
    if (error.message === 'Network Error') {
      // Network Error 可能是：1. 服务器没启动 2. 网络断开 3. CORS问题
      return { 
        type: ErrorType.NETWORK, 
        message: '无法连接到服务器，可能原因：\n1. 后端服务未启动\n2. 网络连接异常\n3. 跨域配置错误',
        detail: error.message 
      };
    }
    return { type: ErrorType.UNKNOWN, message: '发生未知错误', detail: error.message };
  }

  // 有响应对象，根据状态码判断
  const status = error.response.status;
  const statusText = error.response.statusText;

  switch (status) {
    case 0:
      return { 
        type: ErrorType.SERVER_DOWN, 
        message: '服务器无响应，可能原因：\n1. 后端服务未启动\n2. 服务端口配置错误' 
      };
    case 400:
      return { type: ErrorType.BUSINESS, message: '请求参数错误' };
    case 401:
      return { type: ErrorType.AUTH, message: '未授权，请重新登录' };
    case 403:
      return { type: ErrorType.AUTH, message: '拒绝访问，权限不足' };
    case 404:
      return { type: ErrorType.BUSINESS, message: '请求的资源不存在' };
    case 500:
      return { type: ErrorType.BUSINESS, message: '服务器内部错误' };
    case 502:
      return { type: ErrorType.SERVER_DOWN, message: '网关错误，后端服务可能未启动' };
    case 503:
      return { type: ErrorType.SERVER_DOWN, message: '服务暂时不可用，请稍后重试' };
    case 504:
      return { type: ErrorType.TIMEOUT, message: '网关超时，后端服务响应过慢' };
    default:
      return { type: ErrorType.UNKNOWN, message: `请求失败: ${status} ${statusText}` };
  }
}

/**
 * 显示错误信息（支持多行显示）
 */
function showError(title, content) {
  message.destroy();
  Modal.error({
    title: title,
    content: content,
    width: 450,
  });
}

/**
 * 检查后端服务是否可用
 */
let isCheckingHealth = false;
async function checkServerHealth() {
  if (isCheckingHealth) return;
  isCheckingHealth = true;
  
  try {
    const baseUrl = import.meta.env.VITE_APP_API_URL;
    // 去掉 /api 后缀，Gateway actuator 端点位于 /actuator/health
    const gatewayUrl = baseUrl.replace(/\/api$/, '');
    await axios.get(`${gatewayUrl}/actuator/health`, { timeout: 3000 });
  } catch (error) {
    // 如果连健康检查接口都访问不了，说明服务确实有问题
    console.error('[Health Check] 后端服务不可用:', error.message);
  } finally {
    isCheckingHealth = false;
  }
}

// ================================= 请求拦截器 =================================

axiosInstance.interceptors.request.use(
  (config) => {
    // 在发送请求之前消息头加入token token
    const token = localRead(LocalStorageKeyConst.USER_TOKEN);
    if (token) {
      config.headers[TOKEN_HEADER] = 'Bearer ' + token;
    } else {
      delete config.headers[TOKEN_HEADER];
    }

    // 多租户：添加租户ID Header
    const tenantId = localRead(LocalStorageKeyConst.USER_TENANT_ID);
    if (tenantId) {
      config.headers['X-Tenant-Id'] = tenantId;
    }

    // 多租户：平台管理员标识
    const isPlatformAdmin = localRead(LocalStorageKeyConst.USER_PLATFORM_ADMIN);
    if (isPlatformAdmin === 'true') {
      config.headers['X-Platform-Admin'] = 'true';
    }

    return config;
  },
  (error) => {
    // 对请求错误做些什么
    return Promise.reject(error);
  }
);

// ================================= 响应拦截器 =================================

// 添加响应拦截器
axiosInstance.interceptors.response.use(
  (response) => {
    // 根据content-type ，判断是否为 json 数据
    let contentType = response.headers['content-type'] ? response.headers['content-type'] : response.headers['Content-Type'];
    if (contentType.indexOf('application/json') === -1) {
      return Promise.resolve(response);
    }

    // 如果是json数据
    if (response.data && response.data instanceof Blob) {
      // 检查是否为错误响应（Blob 类型但实际是 JSON 错误）
      const contentType = response.headers['content-type'] ? response.headers['content-type'] : response.headers['Content-Type'];
      if (contentType && contentType.indexOf('application/json') !== -1) {
        // 这是一个 JSON 错误响应被错误地转为 Blob，需要解析并显示错误
        return new Promise((resolve, reject) => {
          const reader = new FileReader();
          reader.onload = () => {
            try {
              const errorData = JSON.parse(reader.result);
              // 统一处理错误消息显示
              if (errorData.code && errorData.code !== 0 && errorData.code !== '0') {
                message.destroy();
                message.error(errorData.msg || '操作失败');
              }
              reject(errorData);
            } catch (e) {
              reject(response.data);
            }
          };
          reader.onerror = () => reject(response.data);
          reader.readAsText(response.data);
        });
      }
      return Promise.reject(response.data);
    }

    // 如果是加密数据
    if (response.data.dataType === DATA_TYPE_ENUM.ENCRYPT.value) {
      response.data.encryptData = response.data.data;
      let decryptStr = decryptData(response.data.data);
      if (decryptStr) {
        response.data.data = JSON.parse(decryptStr);
      }
    }

    const res = response.data;
    if (res.code && res.code !== 0 && res.code !== '0') {
      // ============ 认证授权相关错误处理 ============
      // 登录状态失效
      if (res.code === UserErrorCode.LOGIN_STATE_INVALID || res.code === UserErrorCode.DEVELOPING) {
        message.destroy();
        message.error(res.msg || '您还未登录或登录失效，请重新登录！');
        setTimeout(logout, 300);
        return Promise.reject(res);
      }

      // Token无效/过期
      if (res.code === SystemErrorCode.TOKEN_INVALID || res.code === SystemErrorCode.TOKEN_EXPIRED) {
        message.destroy();
        message.error(res.msg || '登录已失效，请重新登录！');
        setTimeout(logout, 300);
        return Promise.reject(res);
      }

      // 权限不足
      if (res.code === UserErrorCode.NO_PERMISSION || res.code === SystemErrorCode.PERMISSION_DENIED) {
        message.destroy();
        message.error(res.msg || '权限不足，无法访问');
        return Promise.reject(res);
      }

      // ============ 登录安全提醒 ============
      if (res.code === UserErrorCode.LOGIN_FAIL_LOCK || res.code === UserErrorCode.LOGIN_FAIL_WILL_LOCK) {
        Modal.error({
          title: '重要提醒',
          content: res.msg,
        });
        return Promise.reject(res);
      }

      // 长时间未操作系统，需要重新登录
      if (res.code === UserErrorCode.LOGIN_ACTIVE_TIMEOUT) {
        Modal.error({
          title: '重要提醒',
          content: res.msg,
          onOk: logout,
        });
        setTimeout(logout, 3000);
        return Promise.reject(res);
      }

      // ============ 系统级错误（需要特别关注） ============
      if (res.code >= 50001 && res.code <= 50999) {
        // 数据库错误
        if (res.code === SystemErrorCode.DB_ERROR) {
          showError('数据库错误', res.msg || '数据库操作失败，请联系管理员');
          return Promise.reject(res);
        }
        
        // 服务不可用
        if (res.code === SystemErrorCode.SERVICE_UNAVAILABLE || res.code === SystemErrorCode.SERVICE_DEGRADATION) {
          showError('服务暂时不可用', res.msg || '服务繁忙，请稍后重试');
          return Promise.reject(res);
        }
        
        // 超时
        if (res.code === SystemErrorCode.SERVICE_TIMEOUT) {
          showError('请求超时', res.msg || '服务响应超时，请稍后重试');
          return Promise.reject(res);
        }

        // 其他系统错误
        showError('系统错误', res.msg || '系统发生错误，请联系管理员');
        return Promise.reject(res);
      }

      // ============ 用户级错误（普通提示） ============
      message.destroy();
      message.error(res.msg || '操作失败');
      return Promise.reject(res);
    } else {
      return Promise.resolve(res);
    }
  },
  (error) => {
    // ============ 网络层错误处理 ============
    console.error('[Axios Error]', error);
    
    const errorInfo = analyzeError(error);

    // 根据错误类型显示不同的提示
    switch (errorInfo.type) {
      case ErrorType.NETWORK:
        showError('网络连接失败', errorInfo.message);
        // 尝试检测服务器状态
        checkServerHealth();
        break;
        
      case ErrorType.TIMEOUT:
        showError('请求超时', errorInfo.message);
        break;
        
      case ErrorType.SERVER_DOWN:
        showError('服务不可用', errorInfo.message);
        break;
        
      case ErrorType.AUTH:
        showError('认证失败', errorInfo.message);
        setTimeout(logout, 2000);
        break;
        
      default:
        message.destroy();
        message.error(errorInfo.message || '网络发生错误');
    }

    // 返回标准化的错误对象
    return Promise.reject({
      code: -1,
      msg: errorInfo.message,
      type: errorInfo.type,
      detail: errorInfo.detail,
      originalError: error,
    });
  }
);

// ================================= 对外提供请求方法：通用请求，get， post, 下载download等 =================================

/**
 * get请求
 */
export const getRequest = (url, params) => {
  return request({ url, method: 'get', params });
};

/**
 * 通用请求封装
 * @param config
 */
export const request = (config) => {
  return axiosInstance.request(config);
};

/**
 * post请求
 */
export const postRequest = (url, data, config = {}) => {
  return request({
    data,
    url,
    method: 'post',
    ...config,
  });
};

/**
 * delete 请求
 */
export const deleteRequest = (url, params) => {
  return request({ url, method: 'delete', params });
};

/**
 * put 请求
 */
export const putRequest = (url, data, config = {}) => {
  return request({
    data,
    url,
    method: 'put',
    ...config,
  });
};

// ================================= 加密 =================================

/**
 * 加密请求参数的post请求
 */
export const postEncryptRequest = (url, data) => {
  return request({
    data: { encryptData: encryptData(data) },
    url,
    method: 'post',
  });
};

// ================================= 下载 =================================

export const postDownload = function (url, data) {
  request({
    method: 'post',
    url,
    data,
    responseType: 'blob',
  })
    .then((data) => {
      handleDownloadData(data);
    })
    .catch((error) => {
      handleDownloadError(error);
    });
};

/**
 * post文件下载 - 返回Promise，支持自定义处理
 */
export const postDownloadPromise = function (url, data) {
  return request({
    method: 'post',
    url,
    data,
    responseType: 'blob',
  });
};

/**
 * 文件下载
 */
export const getDownload = function (url, params) {
  request({
    method: 'get',
    url,
    params,
    responseType: 'blob',
  })
    .then((data) => {
      handleDownloadData(data);
    })
    .catch((error) => {
      handleDownloadError(error);
    });
};

function handleDownloadError(error) {
  if (error instanceof Blob) {
    const fileReader = new FileReader();
    fileReader.readAsText(error);
    fileReader.onload = () => {
      const msg = fileReader.result;
      const jsonMsg = JSON.parse(msg);
      message.destroy();
      message.error(jsonMsg.msg);
    };
  } else {
    message.destroy();
    message.error('网络发生错误', error);
  }
}

/**
 * 从 Content-Disposition 消息头解析文件名
 */
function parseFilename(disposition) {
  let filename = 'download.sql';
  if (disposition) {
    let strArr = disposition.split(';');
    for (let i = 0; i < strArr.length; i++) {
      if (strArr[i].indexOf('filename') >= 0 || strArr[i].indexOf('fileName') >= 0) {
        let namePart = strArr[i].split('=')[1];
        if (namePart) {
          filename = decodeURIComponent(namePart.replace(/"/g, ''));
          break;
        }
      }
    }
  }
  return filename;
}

/**
 * 是否为 Tauri 桌面端运行环境（Tauri v2 会向页面注入 __TAURI_INTERNALS__）
 */
function isDesktopRuntime() {
  return typeof window !== 'undefined' && !!window.__TAURI_INTERNALS__;
}

/**
 * 桌面端保存文件：WebView 中 <a download> 不会触发下载，改为系统保存对话框 + 写本地文件
 */
async function desktopSaveBlob(blob, filename) {
  const [{ save }, { writeFile }] = await Promise.all([import('@tauri-apps/plugin-dialog'), import('@tauri-apps/plugin-fs')]);
  const path = await save({ defaultPath: filename });
  if (!path) {
    return null;
  }
  await writeFile(path, new Uint8Array(await blob.arrayBuffer()));
  return path;
}

function handleDownloadData(response) {
  if (!response) {
    return;
  }

  // 获取返回类型
  let contentType = _.isUndefined(response.headers['content-type']) ? response.headers['Content-Type'] : response.headers['content-type'];

  // 从消息头获取文件名
  let disposition = _.isUndefined(response.headers['content-disposition'])
    ? response.headers['Content-Disposition']
    : response.headers['content-disposition'];

  let filename = parseFilename(disposition);
  let blob = new Blob([response.data], { type: contentType });

  // 桌面端（Tauri）：弹系统保存对话框落盘
  if (isDesktopRuntime()) {
    desktopSaveBlob(blob, filename)
      .then((path) => {
        if (path) {
          message.destroy();
          message.success('已保存：' + path);
        }
      })
      .catch((e) => {
        message.destroy();
        message.error('保存失败：' + (e && e.message ? e.message : e));
      });
    return;
  }

  // 浏览器：构建下载数据并触发点击下载
  let url = window.URL.createObjectURL(blob);
  let link = document.createElement('a');
  link.style.display = 'none';
  link.href = url;
  link.setAttribute('download', filename);

  // 触发点击下载
  document.body.appendChild(link);
  link.click();

  // 下载完释放
  document.body.removeChild(link); // 下载完成移除元素
  window.URL.revokeObjectURL(url); // 释放掉blob对象
}

// 导出错误码，方便其他模块使用
export { SystemErrorCode, UserErrorCode, ErrorType, analyzeError };
