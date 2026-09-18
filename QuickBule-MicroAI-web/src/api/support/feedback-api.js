/*
 * 意见反馈
 *

 */
import { postRequest } from '/@/lib/axios';

export const feedbackApi = {
    // 意见反馈-新增
    addFeedback: (params) => {
        return postRequest('/support/feedback/add', params);
    },
    // 意见反馈-分页查询
    queryFeedback: (params) => {
        return postRequest('/support/feedback/query', params);
    },
};