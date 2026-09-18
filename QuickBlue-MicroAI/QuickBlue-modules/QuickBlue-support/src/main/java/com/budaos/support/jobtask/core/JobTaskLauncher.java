package com.budaos.support.jobtask.core;

import com.budaos.support.constant.JobTaskConst;
import com.budaos.support.util.JobTaskUtil;
import com.budaos.support.jobtask.config.JobTaskConfig;
import com.budaos.support.jobtask.repository.JobTaskRepository;
import com.budaos.support.domain.entity.JobTaskEntity;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 定时任务 作业启动类
 * 负责启动和管理所有定时任务
 *
 * @author budaos
 */
@Slf4j
public class JobTaskLauncher {

    private final JobTaskRepository jobRepository;

    private final List<JobTask> jobInterfaceList;

    private final RedissonClient redissonClient;

    public JobTaskLauncher(JobTaskConfig jobConfig,
                            JobTaskRepository jobRepository,
                            List<JobTask> jobInterfaceList,
                            RedissonClient redissonClient) {
        this.jobRepository = jobRepository;
        this.jobInterfaceList = jobInterfaceList;
        this.redissonClient = redissonClient;

        // init job scheduler
        JobTaskScheduler.init(jobConfig);

        // 任务自动检测配置 固定1个线程
        Integer initDelay = jobConfig.getInitDelay();
        Boolean refreshEnabled = jobConfig.getDbRefreshEnabled();
        Integer refreshInterval = jobConfig.getDbRefreshInterval();

        ThreadFactory factory = new ThreadFactoryBuilder().setNameFormat("JobTaskLauncher-%d").build();
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1, factory);
        Runnable launcherRunnable = () -> {
            try {
                // 查询所有任务
                List<JobTaskEntity> jobList = this.queryJob();
                this.startOrRefreshJob(jobList);
            } catch (Throwable t) {
                log.error("JobTask Error:", t);
            }
            // 只在启动时 执行一次
            if (!refreshEnabled) {
                executor.shutdown();
            }
        };
        executor.scheduleWithFixedDelay(launcherRunnable, initDelay, refreshInterval, TimeUnit.SECONDS);

        // 打印信息
        String refreshDesc = refreshEnabled ? "开启|检测间隔" + refreshInterval + "秒" : "关闭";
        String format = String.format(JobTaskConst.LOGO, jobConfig.getCorePoolSize(), initDelay, refreshDesc);
        JobTaskUtil.printInfo(format);
    }

    /**
     * 查询数据库
     * 启动/刷新任务
     */
    public void startOrRefreshJob(List<JobTaskEntity> jobList) {
        // 查询任务配置
        if (CollectionUtils.isEmpty(jobList) || CollectionUtils.isEmpty(jobInterfaceList)) {
            log.info("==== JobTask ==== job list empty");
            return;
        }

        // 任务实现类
        Map<String, JobTask> jobImplMap = jobInterfaceList.stream().collect(Collectors.toMap(JobTask::getClassName, Function.identity()));
        for (JobTaskEntity jobEntity : jobList) {
            // 任务是否存在 判断是否需要更新
            Integer jobId = jobEntity.getJobId();
            JobTaskEntity oldJobEntity = JobTaskScheduler.getJobInfo(jobId);
            if (null != oldJobEntity) {
                // 不需要更新
                if (!isNeedUpdate(oldJobEntity, jobEntity)) {
                    continue;
                }
                // 需要更新 移除原任务
                JobTaskScheduler.removeJob(jobId);
            }
            // 任务未开启
            if (!jobEntity.getEnabledFlag()) {
                continue;
            }
            // 任务删除
            if (jobEntity.getDeletedFlag()) {
                continue;
            }
            // 查找任务实现类
            JobTask jobImpl = jobImplMap.get(jobEntity.getJobClass());
            if (null == jobImpl) {
                continue;
            }
            // 添加任务
            JobTaskExecutor jobExecute = new JobTaskExecutor(jobEntity, jobRepository, jobImpl, redissonClient);
            JobTaskScheduler.addJob(jobExecute);
        }
        List<JobTaskEntity> runjJobList = JobTaskScheduler.getJobInfo();
        List<String> jobNameList = runjJobList.stream().map(JobTaskEntity::getJobName).collect(Collectors.toList());
        log.info("==== JobTask ==== start/refresh job num:{}->{}", runjJobList.size(), jobNameList);
    }

    /**
     * 查询全部任务
     *
     * @return 任务列表
     */
    private List<JobTaskEntity> queryJob() {
        return jobRepository.getJobDao().selectList(null);
    }

    /**
     * 手动判断 任务配置 是否需要更新
     * 新增字段的话 在这个方法里增加判断
     *
     * @param oldJob 旧任务
     * @param newJob 新任务
     * @return 是否需要更新
     */
    private static boolean isNeedUpdate(JobTaskEntity oldJob, JobTaskEntity newJob) {
        // cron为空时 fixedDelay 才有意义
        return !Objects.equals(oldJob.getEnabledFlag(), newJob.getEnabledFlag())
                || !Objects.equals(oldJob.getDeletedFlag(), newJob.getDeletedFlag())
                || !Objects.equals(oldJob.getTriggerType(), newJob.getTriggerType())
                || !Objects.equals(oldJob.getTriggerValue(), newJob.getTriggerValue())
                || !Objects.equals(oldJob.getJobClass(), newJob.getJobClass());
    }

    @PreDestroy
    public void destroy() {
        JobTaskScheduler.destroy();
        log.info("==== JobTask ==== destroy job");
    }
}
