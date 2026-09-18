/**
 * 错误码常量定义
 * 与后端 com.budaos.common.core.code 包下的错误码保持一致
 */

/**
 * 系统级错误码（50001-50999）
 * 这类错误通常需要关注和处理
 */
export const SYSTEM_ERROR_CODE = {
  // 基础系统错误
  SYSTEM_ERROR: 50001,          // 系统错误
  DB_ERROR: 50002,              // 数据库错误
  FILE_ERROR: 50004,            // 文件操作失败
  CONFIG_ERROR: 50005,          // 配置错误

  // 网络相关错误
  SERVICE_UNAVAILABLE: 50100,   // 服务不可用
  SERVICE_TIMEOUT: 50101,       // 服务超时
  NETWORK_CONNECT_ERROR: 50102, // 网络连接失败
  GATEWAY_ERROR: 50103,         // 网关错误
  SERVICE_DEGRADATION: 50104,   // 服务降级

  // 认证授权相关
  TOKEN_INVALID: 50200,         // Token无效
  TOKEN_EXPIRED: 50201,         // Token过期
  PERMISSION_DENIED: 50202,     // 权限不足

  // 业务处理错误
  BUSINESS_ERROR: 50300,        // 业务处理异常
  DATA_VALIDATE_ERROR: 50301,   // 数据校验失败
  CONCURRENT_ERROR: 50302,      // 并发操作冲突
};

/**
 * 用户级错误码（30001-30999）
 * 这类错误通常是用户操作引起的
 */
export const USER_ERROR_CODE = {
  PARAM_ERROR: 30001,           // 参数错误
  DATA_NOT_EXIST: 30002,        // 数据不存在
  DATA_DELETED: 30003,          // 数据已删除
  ALREADY_EXIST: 30004,         // 数据已存在
  REPEAT_SUBMIT: 30005,         // 重复提交
  NO_PERMISSION: 30006,         // 无权限
  DEVELOPING: 30007,            // 开发中
  LOGIN_STATE_INVALID: 30008,   // 登录状态无效
  USER_STATUS_ERROR: 30009,     // 用户状态异常
  FORM_REPEAT_SUBMIT: 30010,    // 表单重复提交
  LOGIN_FAIL_LOCK: 30011,       // 登录失败锁定
  LOGIN_FAIL_WILL_LOCK: 30012,  // 登录失败即将锁定
  LOGIN_ACTIVE_TIMEOUT: 30013,  // 长时间未操作
};

/**
 * HTTP状态码对应的消息
 */
export const HTTP_STATUS_MSG = {
  0: '服务器无响应，可能未启动',
  400: '请求参数错误',
  401: '未授权，请重新登录',
  403: '拒绝访问，权限不足',
  404: '请求的资源不存在',
  500: '服务器内部错误',
  502: '网关错误，后端服务可能未启动',
  503: '服务暂时不可用',
  504: '网关超时',
};

/**
 * 判断是否为系统级错误
 */
export function isSystemError(code) {
  return code >= 50001 && code <= 50999;
}

/**
 * 判断是否为用户级错误
 */
export function isUserError(code) {
  return code >= 30001 && code <= 30999;
}

/**
 * 判断是否为认证相关错误
 */
export function isAuthError(code) {
  return code === USER_ERROR_CODE.LOGIN_STATE_INVALID 
    || code === USER_ERROR_CODE.DEVELOPING
    || code === SYSTEM_ERROR_CODE.TOKEN_INVALID
    || code === SYSTEM_ERROR_CODE.TOKEN_EXPIRED;
}
