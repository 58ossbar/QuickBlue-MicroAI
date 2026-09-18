package com.budaos.support.jobtask.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.CurrentUser;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.support.domain.form.JobTaskAddForm;
import com.budaos.support.domain.form.JobTaskEnabledUpdateForm;
import com.budaos.support.domain.form.JobTaskExecuteForm;
import com.budaos.support.domain.form.JobTaskLogQueryForm;
import com.budaos.support.domain.form.JobTaskQueryForm;
import com.budaos.support.domain.form.JobTaskUpdateForm;
import com.budaos.support.domain.vo.JobTaskLogVO;
import com.budaos.support.domain.vo.JobTaskVO;

/**
 * 定时任务 Service
 *
 */
public interface JobTaskService {

    /**
     * 查询 定时任务详情
     *
     * @param jobId
     * @return
     */
    ApiResult<JobTaskVO> queryJobInfo(Integer jobId);

    /**
     * 分页查询 定时任务
     *
     * @param queryForm
     * @return
     */
    ApiResult<PageResponse<JobTaskVO>> queryJob(JobTaskQueryForm queryForm);

    /**
     * 分页查询 定时任务-执行记录
     *
     * @param queryForm
     * @return
     */
    ApiResult<PageResponse<JobTaskLogVO>> queryJobLog(JobTaskLogQueryForm queryForm);

    /**
     * 添加定时任务
     *
     * @param addForm
     * @return
     */
    ApiResult<String> addJob(JobTaskAddForm addForm);

    /**
     * 更新定时任务
     *
     * @param updateForm
     * @return
     */
    ApiResult<String> updateJob(JobTaskUpdateForm updateForm);

    /**
     * 更新定时任务-是否开启
     *
     * @param updateForm
     * @return
     */
    ApiResult<String> updateJobEnabled(JobTaskEnabledUpdateForm updateForm);

    /**
     * 执行定时任务
     * 忽略任务的开启状态,立即执行一次
     *
     * @param executeForm
     * @return
     */
    ApiResult<String> execute(JobTaskExecuteForm executeForm);

    /**
     * 移除定时任务
     * 物理删除
     *
     * @return
     */
    ApiResult<String> deleteJob(Integer jobId, CurrentUser requestUser);
}
