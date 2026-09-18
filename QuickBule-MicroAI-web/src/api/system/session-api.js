/*
 * 会话管理 API
 * 包含：在线用户管理、登录日志
 *
 */
import { postRequest, getRequest } from '/src/lib/axios';

// ==================== 在线用户管理 ====================

export const sessionApi = {
  // 分页查询在线用户
  queryOnlineUserPage: (param) => {
    return postRequest('/system/online/page/query', param);
  },

  // 获取所有在线用户
  queryOnlineUserList: () => {
    return getRequest('/system/online/list');
  },

  // 获取在线用户数量
  getOnlineUserCount: () => {
    return getRequest('/system/online/count');
  },

  // 强制下线
  forceLogout: (userId, userType) => {
    return postRequest('/system/online/forceLogout', null, { params: { userId, userType } });
  },

  // 批量强制下线
  batchForceLogout: (userIds, userType) => {
    return postRequest('/system/online/batchForceLogout', null, { params: { userIds: userIds.join(','), userType } });
  },
};
