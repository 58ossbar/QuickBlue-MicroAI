import { getRequest, postRequest } from '/@/lib/axios';

export const messageApi = {
  // 通知消息-分页查询
  queryMessage: (param) => {
    return postRequest('/support/message/queryMyMessage', param);
  },
  // 通知消息-查询未读消息数
  queryUnreadCount: () => {
    return getRequest('/support/message/getUnreadCount');
  },
  // 通知消息-标记已读
  updateReadFlag: (messageId) => {
    return getRequest(`/support/message/read/${messageId}`);
  },

  //通知消息-分页查询
  queryAdminMessage: (param) => {
    return postRequest('/support/message/query', param);
  },

  //通知消息-新建
  sendMessages: (param) => {
    return postRequest('/support/message/sendMessages', param);
  },

  //通知消息-删除
  deleteMessage: (messageId) => {
    return getRequest(`/support/message/delete/${messageId}`);
  },
};
