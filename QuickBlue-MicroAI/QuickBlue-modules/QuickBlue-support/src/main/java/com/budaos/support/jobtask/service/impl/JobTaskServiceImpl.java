package com.budaos.support.jobtask.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.CurrentUser;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.common.core.code.UserErrorCodes;
import com.budaos.common.core.enums.JobTaskTriggerTypeEnum;
import com.budaos.common.core.util.BeanCopyUtil;
import com.budaos.common.core.util.PageConvertUtil;
import com.budaos.support.util.JobTaskUtil;
import com.budaos.support.domain.entity.JobTaskEntity;
import com.budaos.support.domain.entity.JobTaskLogEntity;
import com.budaos.support.domain.form.*;
import com.budaos.support.domain.vo.JobTaskLogVO;
import com.budaos.support.domain.vo.JobTaskMessage;
import com.budaos.support.domain.vo.JobTaskVO;
import com.budaos.support.jobtask.client.JobTaskClientManager;
import com.budaos.support.jobtask.dao.JobTaskDao;
import com.budaos.support.jobtask.dao.JobTaskLogDao;
import com.budaos.support.jobtask.service.JobTaskService;
import com.google.common.collect.Lists;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 定时任务 接口业务管理
 * 如果不需要通过接口管理定时任务 可以删除此类
 *
 */
@Service
public class JobTaskServiceImpl implements JobTaskService {

    @Resource
    private JobTaskDao jobDao;

    @Resource
    private JobTaskLogDao jobLogDao;

    @Resource
    private JobTaskClientManager jobClientManager;

    /**
     * 查询 定时任务详情
     *
     * @param jobId
     * @return
     */
    @Override
    public ApiResult<JobTaskVO> queryJobInfo(Integer jobId) {
        JobTaskEntity jobEntity = jobDao.selectById(jobId);
        if (null == jobEntity) {
            return ApiResult.error(UserErrorCodes.DATA_NOT_EXIST);
        }
        JobTaskVO jobVO = BeanCopyUtil.copy(jobEntity, JobTaskVO.class);
        // 处理设置job详情
        this.handleJobInfo(Lists.newArrayList(jobVO));
        return ApiResult.ok(jobVO);
    }

    /**
     * 分页查询 定时任务
     *
     * @param queryForm
     * @return
     */
    @Override
    public ApiResult<PageResponse<JobTaskVO>> queryJob(JobTaskQueryForm queryForm) {
        Page<?> page = PageConvertUtil.convert2PageQuery(queryForm);
        List<JobTaskVO> jobList = jobDao.query(page, queryForm);
        PageResponse<JobTaskVO> pageResult = PageConvertUtil.convert2PageResult(page, jobList);
        // 处理设置job详情
        this.handleJobInfo(jobList);
        return ApiResult.ok(pageResult);
    }

    /**
     * 处理设置 任务信息
     *
     * @param jobList
     */
    private void handleJobInfo(List<JobTaskVO> jobList) {
        if (CollectionUtils.isEmpty(jobList)) {
            return;
        }
        // 查询最后一次执行记录
        List<Long> logIdList = jobList.stream().map(JobTaskVO::getLastExecuteLogId).filter(Objects::nonNull).collect(Collectors.toList());
        Map<Long, JobTaskLogVO> lastLogMap = Collections.emptyMap();
        if (CollectionUtils.isNotEmpty(logIdList)) {
            lastLogMap = jobLogDao.selectBatchIds(logIdList)
                    .stream()
                    .collect(Collectors.toMap(JobTaskLogEntity::getLogId, e -> BeanCopyUtil.copy(e, JobTaskLogVO.class)));
        }

        // 循环处理任务信息
        for (JobTaskVO jobVO : jobList) {
            // 设置最后一次执行记录
            Long lastExecuteLogId = jobVO.getLastExecuteLogId();
            if (null != lastExecuteLogId) {
                jobVO.setLastJobLog(lastLogMap.get(lastExecuteLogId));
            }
            // 计算未来5次执行时间
            if (jobVO.getEnabledFlag()) {
                List<LocalDateTime> nextTimeList = JobTaskUtil.queryNextTimeFromNow(jobVO.getTriggerType(), jobVO.getTriggerValue(), jobVO.getLastExecuteTime(), 5);
                jobVO.setNextJobExecuteTimeList(nextTimeList);
            }
        }
    }

    /**
     * 分页查询 定时任务-执行记录
     *
     * @param queryForm
     * @return
     */
    @Override
    public ApiResult<PageResponse<JobTaskLogVO>> queryJobLog(JobTaskLogQueryForm queryForm) {
        Page<?> page = PageConvertUtil.convert2PageQuery(queryForm);
        List<JobTaskLogVO> jobList = jobLogDao.query(page, queryForm);
        PageResponse<JobTaskLogVO> pageResult = PageConvertUtil.convert2PageResult(page, jobList);
        return ApiResult.ok(pageResult);
    }

    /**
     * 添加定时任务
     *
     * @param addForm
     * @return
     */
    @Override
    public synchronized ApiResult<String> addJob(JobTaskAddForm addForm) {
        // 校验参数
        ApiResult<String> checkRes = this.checkParam(addForm);
        if (!checkRes.getOk()) {
            return checkRes;
        }

        // 校验重复的执行类
        JobTaskEntity existJobClass = jobDao.selectByJobClass(addForm.getJobClass());
        if (null != existJobClass && !existJobClass.getDeletedFlag()) {
            return ApiResult.userErrorParam("已经存在相同的执行类");
        }

        // 添加数据
        JobTaskEntity jobEntity = BeanCopyUtil.copy(addForm, JobTaskEntity.class);
        jobDao.insert(jobEntity);

        // 更新执行端
        JobTaskMessage jobMsg = new JobTaskMessage();
        jobMsg.setJobId(jobEntity.getJobId());
        jobMsg.setMsgType(JobTaskMessage.MsgTypeEnum.UPDATE_JOB);
        jobMsg.setUpdateName(addForm.getUpdateName());
        jobClientManager.publishToClient(jobMsg);
        return ApiResult.ok();
    }

    /**
     * 更新定时任务
     *
     * @param updateForm
     * @return
     */
    @Override
    public synchronized ApiResult<String> updateJob(JobTaskUpdateForm updateForm) {
        // 校验参数
        Integer jobId = updateForm.getJobId();
        JobTaskEntity jobEntity = jobDao.selectById(jobId);
        if (null == jobEntity) {
            return ApiResult.error(UserErrorCodes.DATA_NOT_EXIST);
        }

        ApiResult<String> checkRes = this.checkParam(updateForm);
        if (!checkRes.getOk()) {
            return checkRes;
        }

        // 校验重复的执行类
        JobTaskEntity existJobClass = jobDao.selectByJobClass(updateForm.getJobClass());
        if (null != existJobClass && !existJobClass.getDeletedFlag() && !existJobClass.getJobId().equals(jobId)) {
            return ApiResult.userErrorParam("已经存在相同的执行类");
        }

        // 更新数据
        jobEntity = BeanCopyUtil.copy(updateForm, JobTaskEntity.class);
        jobDao.updateById(jobEntity);

        // 更新执行端
        JobTaskMessage jobMsg = new JobTaskMessage();
        jobMsg.setJobId(jobId);
        jobMsg.setMsgType(JobTaskMessage.MsgTypeEnum.UPDATE_JOB);
        jobMsg.setUpdateName(updateForm.getUpdateName());
        jobClientManager.publishToClient(jobMsg);
        return ApiResult.ok();
    }

    /**
     * 校验参数
     * 如需其他校验，请自行添加校验逻辑
     *
     * @param addForm
     * @return
     */
    private ApiResult<String> checkParam(JobTaskAddForm addForm) {
        // 校验触发时间配置
        String triggerType = addForm.getTriggerType();
        String triggerValue = addForm.getTriggerValue();
        if (JobTaskTriggerTypeEnum.CRON.equalsValue(triggerType) && !JobTaskUtil.checkCron(triggerValue)) {
            return ApiResult.userErrorParam("cron表达式错误");
        }
        if (JobTaskTriggerTypeEnum.FIXED_DELAY.equalsValue(triggerType) && !JobTaskUtil.checkFixedDelay(triggerValue)) {
            return ApiResult.userErrorParam("固定间隔配置错误：必须是大于0的整数");
        }
        // 校验job class
        return JobTaskUtil.checkJobClass(addForm.getJobClass());
    }

    /**
     * 更新定时任务-是否开启
     *
     * @param updateForm
     * @return
     */
    @Override
    public ApiResult<String> updateJobEnabled(JobTaskEnabledUpdateForm updateForm) {
        Integer jobId = updateForm.getJobId();
        JobTaskEntity jobEntity = jobDao.selectById(jobId);
        if (null == jobEntity) {
            return ApiResult.error(UserErrorCodes.DATA_NOT_EXIST);
        }
        Boolean enabledFlag = updateForm.getEnabledFlag();
        if (Objects.equals(enabledFlag, jobEntity.getEnabledFlag())) {
            return ApiResult.ok();
        }
        // 更新数据
        jobEntity = new JobTaskEntity();
        jobEntity.setJobId(jobId);
        jobEntity.setEnabledFlag(enabledFlag);
        jobEntity.setUpdateName(updateForm.getUpdateName());
        jobDao.updateById(jobEntity);

        // 更新执行端
        JobTaskMessage jobMsg = new JobTaskMessage();
        jobMsg.setJobId(jobId);
        jobMsg.setMsgType(JobTaskMessage.MsgTypeEnum.UPDATE_JOB);
        jobMsg.setUpdateName(updateForm.getUpdateName());
        jobClientManager.publishToClient(jobMsg);
        return ApiResult.ok();
    }

    /**
     * 执行定时任务
     * 忽略任务的开启状态,立即执行一次
     *
     * @param executeForm
     * @return
     */
    @Override
    public ApiResult<String> execute(JobTaskExecuteForm executeForm) {
        Integer jobId = executeForm.getJobId();
        JobTaskEntity jobEntity = jobDao.selectById(jobId);
        if (null == jobEntity) {
            return ApiResult.error(UserErrorCodes.DATA_NOT_EXIST);
        }

        // 更新执行端
        JobTaskMessage jobMsg = new JobTaskMessage();
        jobMsg.setJobId(jobId);
        jobMsg.setParam(executeForm.getParam());
        jobMsg.setMsgType(JobTaskMessage.MsgTypeEnum.EXECUTE_JOB);
        jobMsg.setUpdateName(executeForm.getUpdateName());
        jobClientManager.publishToClient(jobMsg);
        return ApiResult.ok();
    }

    /**
     * 移除定时任务
     * 物理删除
     *
     * @return
     */
    @Override
    public synchronized ApiResult<String> deleteJob(Integer jobId, CurrentUser requestUser) {
        // 删除任务
        jobDao.updateDeletedFlag(jobId, Boolean.TRUE);

        // 更新执行端
        JobTaskMessage jobMsg = new JobTaskMessage();
        jobMsg.setJobId(jobId);
        jobMsg.setMsgType(JobTaskMessage.MsgTypeEnum.UPDATE_JOB);
        jobMsg.setUpdateName(requestUser.getUserName());
        jobClientManager.publishToClient(jobMsg);
        return ApiResult.ok();
    }
}
