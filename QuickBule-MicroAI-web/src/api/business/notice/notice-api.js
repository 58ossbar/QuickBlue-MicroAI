/*
 * @Description: 公告信息、企业动态
 */
import { postRequest, getRequest } from '/@/lib/axios';

export const noticeApi = {
  // ---------------- 通知公告类型 -----------------------

  // 通知公告类型-获取全部 
  getAllNoticeTypeList() {
    return getRequest('/oa/noticeType/getAll');
  },

  // 通知公告类型-添加 
  addNoticeType(name) {
    return getRequest(`/oa/noticeType/add/${name}`);
  },

  // 通知公告类型-修改 
  updateNoticeType(noticeTypeId, name) {
    return getRequest(`/oa/noticeType/update/${noticeTypeId}/${name}`);
  },
  // 通知公告类型-删除 
  deleteNoticeType(noticeTypeId) {
    return getRequest(`/oa/noticeType/delete/${noticeTypeId}`);
  },

  // ---------------- 通知公告管理 -----------------------

  // 通知公告-分页查询 
  queryNotice(param) {
    return postRequest('/oa/notice/query', param);
  },

  // 通知公告-添加 
  addNotice(param) {
    return postRequest('/oa/notice/add', param);
  },

  // 通知公告-更新 
  updateNotice(param) {
    return postRequest('/oa/notice/update', param);
  },
    // 通知公告-审核 
    auditNotice(param) {
        return postRequest('/oa/notice/audit', param);
    },

    // 通知公告-批量审核 
    batchAuditNotice(param) {
        return postRequest('/oa/notice/batchAudit', param);
    },
  // 通知公告-删除 
  deleteNotice(noticeId) {
    return getRequest(`/oa/notice/delete/${noticeId}`);
  },

  // 通知公告-更新详情 
  getUpdateNoticeInfo(noticeId) {
    return getRequest(`/oa/notice/getUpdateVO/${noticeId}`);
  },

  // --------------------- 【员工】查看 通知公告 -------------------------

  // 通知公告-员工-查看详情 
  view(noticeId) {
    return getRequest(`/oa/notice/employee/view/${noticeId}`);
  },

  // 通知公告-员工-查询 
  queryEmployeeNotice(param) {
    return postRequest('/oa/notice/employee/query', param);
  },

  // 【员工】通知公告-查询 查看记录 
  queryViewRecord(param) {
    return postRequest('/oa/notice/employee/queryViewRecord', param);
  },
};
