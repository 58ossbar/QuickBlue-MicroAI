package com.budaos.support.jobtask.controller;

import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.CurrentUser;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.common.core.util.RequestContextUtil;
import com.budaos.support.constant.AdminApiTagConst;
import com.budaos.support.domain.form.JobTaskAddForm;
import com.budaos.support.domain.form.JobTaskEnabledUpdateForm;
import com.budaos.support.domain.form.JobTaskExecuteForm;
import com.budaos.support.domain.form.JobTaskLogQueryForm;
import com.budaos.support.domain.form.JobTaskQueryForm;
import com.budaos.support.domain.form.JobTaskUpdateForm;
import com.budaos.support.domain.vo.JobTaskLogVO;
import com.budaos.support.domain.vo.JobTaskVO;
import com.budaos.support.jobtask.config.JobTaskAutoConfiguration;
import com.budaos.support.jobtask.service.JobTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.web.bind.annotation.*;

/**
 * 定时任务 管理接口
 *
 */
@Tag(name = AdminApiTagConst.Support.JOB)
@RestController
public class JobTaskController {

    @Autowired
    private JobTaskService jobService;

    @Operation(summary = "定时任务-立即执行")
    @PostMapping("/job/execute")
    public ApiResult<String> execute(@RequestBody @Valid JobTaskExecuteForm executeForm) {
        CurrentUser requestUser = RequestContextUtil.getRequestUser();
        executeForm.setUpdateName(requestUser.getUserName());
        return jobService.execute(executeForm);
    }

    @Operation(summary = "定时任务-查询详情")
    @GetMapping("/job/{jobId}")
    public ApiResult<JobTaskVO> queryJobInfo(@PathVariable Integer jobId) {
        return jobService.queryJobInfo(jobId);
    }

    @Operation(summary = "定时任务-分页查询")
    @PostMapping("/job/query")
    public ApiResult<PageResponse<JobTaskVO>> queryJob(@RequestBody @Valid JobTaskQueryForm queryForm) {
        return jobService.queryJob(queryForm);
    }

    @Operation(summary = "定时任务-分页查询(GET)")
    @GetMapping("/job/query")
    public ApiResult<PageResponse<JobTaskVO>> queryJobGet(@RequestParam(required = false) String searchWord,
                                                             @RequestParam(required = false) String triggerType,
                                                             @RequestParam(required = false) Boolean enabledFlag,
                                                             @RequestParam(required = false) Boolean deletedFlag,
                                                             @RequestParam(defaultValue = "1") Integer pageNum,
                                                             @RequestParam(defaultValue = "10") Integer pageSize) {
        JobTaskQueryForm queryForm = new JobTaskQueryForm();
        queryForm.setSearchWord(searchWord);
        queryForm.setTriggerType(triggerType);
        queryForm.setEnabledFlag(enabledFlag);
        queryForm.setDeletedFlag(deletedFlag);
        queryForm.setPageNum(Long.valueOf(pageNum));
        queryForm.setPageSize(Long.valueOf(pageSize));
        return jobService.queryJob(queryForm);
    }

    @Operation(summary = "定时任务-添加任务")
    @PostMapping("/job/add")
    public ApiResult<String> addJob(@RequestBody @Valid JobTaskAddForm addForm) {
        CurrentUser requestUser = RequestContextUtil.getRequestUser();
        addForm.setUpdateName(requestUser.getUserName());
        return jobService.addJob(addForm);
    }

    @Operation(summary = "定时任务-更新-任务信息")
    @PostMapping("/job/update")
    public ApiResult<String> updateJob(@RequestBody @Valid JobTaskUpdateForm updateForm) {
        CurrentUser requestUser = RequestContextUtil.getRequestUser();
        updateForm.setUpdateName(requestUser.getUserName());
        return jobService.updateJob(updateForm);
    }

    @Operation(summary = "定时任务-更新-开启状态")
    @PostMapping("/job/update/enabled")
    public ApiResult<String> updateJobEnabled(@RequestBody @Valid JobTaskEnabledUpdateForm updateForm) {
        CurrentUser requestUser = RequestContextUtil.getRequestUser();
        updateForm.setUpdateName(requestUser.getUserName());
        return jobService.updateJobEnabled(updateForm);
    }

    @Operation(summary = "定时任务-删除")
    @GetMapping("/job/delete")
    public ApiResult<String> deleteJob(@RequestParam Integer jobId) {
        return jobService.deleteJob(jobId, RequestContextUtil.getRequestUser());
    }

    @Operation(summary = "定时任务-执行记录-分页查询")
    @PostMapping("/job/log/query")
    public ApiResult<PageResponse<JobTaskLogVO>> queryJobLog(@RequestBody @Valid JobTaskLogQueryForm queryForm) {
        return jobService.queryJobLog(queryForm);
    }
}
