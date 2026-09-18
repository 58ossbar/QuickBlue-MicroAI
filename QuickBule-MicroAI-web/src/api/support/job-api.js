/*
 * job api
 *

 */
import { postRequest, getRequest } from '/@/lib/axios';

export const jobApi = {
    // 分页查询
    queryJob: (param) => {
        return postRequest('/support/job/query', param);
    },
    // 定时任务-查询详情
    queryJobInfo: (param) => {
        return getRequest(`/support/job/${param}`);
    },
    // 执行任务
    executeJob: (param) => {
        return postRequest('/support/job/execute', param);
    },
    // 定时任务-新增-任务信息
    addJob: (param) => {
        return postRequest('/support/job/add', param);
    },
    // 定时任务-更新-任务信息
    updateJob: (param) => {
        return postRequest('/support/job/update', param);
    },
    // 定时任务-更新-开启状态
    updateJobEnabled: (param) => {
        return postRequest('/support/job/update/enabled', param);
    },
    // 定时任务-执行记录-分页查询
    queryJobLog: (param) => {
        return postRequest('/support/job/log/query', param);
    },
    // 定时任务-删除
    deleteJob: (param) => {
        return getRequest(`/support/job/delete?jobId=${param}`);
    },
};