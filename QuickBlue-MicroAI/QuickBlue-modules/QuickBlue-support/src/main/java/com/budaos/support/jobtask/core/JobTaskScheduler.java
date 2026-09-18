package com.budaos.support.jobtask.core;

import com.budaos.support.util.JobTaskUtil;
import com.budaos.support.jobtask.config.JobTaskConfig;
import com.budaos.common.core.enums.JobTaskTriggerTypeEnum;
import com.budaos.support.domain.entity.JobTaskEntity;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.PeriodicTrigger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 定时任务 调度管理
 * 负责管理所有定时任务的调度、添加、删除和执行
 *
 * @author budaos
 */
@Slf4j
public class JobTaskScheduler {

    /**
     * Spring线程池任务调度器
     */
    private static ThreadPoolTaskScheduler TASK_SCHEDULER;

    /**
     * 定时任务 map (jobId -> (JobEntity, ScheduledFuture))
     */
    private static Map<Integer, Pair<JobTaskEntity, ScheduledFuture<?>>> JOB_FUTURE_MAP;

    private JobTaskScheduler() {

    }

    /**
     * 初始化任务调度配置
     *
     * @param config 配置对象
     */
    public static void init(JobTaskConfig config) {
        TASK_SCHEDULER = new ThreadPoolTaskScheduler();
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat("JobTaskExecutor-%d")
                .build();
        TASK_SCHEDULER.setThreadFactory(threadFactory);
        TASK_SCHEDULER.setPoolSize(config.getCorePoolSize());
        // 线程池在关闭时会等待所有任务完成
        TASK_SCHEDULER.setWaitForTasksToCompleteOnShutdown(true);
        // 在调用shutdown方法后，等待任务完成的最长时间
        TASK_SCHEDULER.setAwaitTerminationSeconds(10);
        // 错误处理
        TASK_SCHEDULER.setErrorHandler((t) -> log.error("JobTaskExecute Err:", t));
        // 当一个任务在被调度执行前被取消时，是否应该从线程池的任务队列中移除
        TASK_SCHEDULER.setRemoveOnCancelPolicy(true);
        TASK_SCHEDULER.initialize();

        JOB_FUTURE_MAP = new ConcurrentHashMap<>();
        log.info("JobTaskScheduler 初始化完成，线程池大小: {}", config.getCorePoolSize());
    }

    /**
     * 获取任务执行对象
     *
     * @param jobId 任务ID
     * @return ScheduledFuture
     */
    public static ScheduledFuture<?> getJobFuture(Integer jobId) {
        Pair<JobTaskEntity, ScheduledFuture<?>> pair = JOB_FUTURE_MAP.get(jobId);
        if (null == pair) {
            return null;
        }
        return pair.getRight();
    }

    /**
     * 获取当前所有执行任务
     *
     * @return 任务列表
     */
    public static List<JobTaskEntity> getJobInfo() {
        return JOB_FUTURE_MAP.values().stream()
                .map(Pair::getLeft)
                .collect(Collectors.toList());
    }

    /**
     * 获取任务执行实体类
     *
     * @param jobId 任务ID
     * @return 任务实体
     */
    public static JobTaskEntity getJobInfo(Integer jobId) {
        Pair<JobTaskEntity, ScheduledFuture<?>> pair = JOB_FUTURE_MAP.get(jobId);
        if (null == pair) {
            return null;
        }
        return pair.getLeft();
    }

    /**
     * 添加任务
     *
     * @param jobExecutor 任务执行器
     */
    public static void addJob(JobTaskExecutor jobExecutor) {
        // 任务是否存在
        JobTaskEntity jobEntity = jobExecutor.getJob();
        Integer jobId = jobEntity.getJobId();
        if (JOB_FUTURE_MAP.containsKey(jobId)) {
            // 移除旧任务
            removeJob(jobId);
        }
        // 任务触发类型
        Trigger trigger = null;
        String triggerType = jobEntity.getTriggerType();
        String triggerValue = jobEntity.getTriggerValue();
        // 优先 cron 表达式
        if (JobTaskTriggerTypeEnum.CRON.equalsValue(triggerType)) {
            trigger = new CronTrigger(triggerValue);
        } else if (JobTaskTriggerTypeEnum.FIXED_DELAY.equalsValue(triggerType)) {
            trigger = new PeriodicTrigger(JobTaskUtil.getFixedDelayVal(triggerValue), TimeUnit.SECONDS);
        }
        String jobName = jobEntity.getJobName();
        if (null == trigger) {
            log.error("==== JobTask ==== trigger-value not null {}", jobName);
            return;
        }
        // 执行任务
        ScheduledFuture<?> schedule = TASK_SCHEDULER.schedule(jobExecutor, trigger);
        JOB_FUTURE_MAP.put(jobId, Pair.of(jobEntity, schedule));
        log.info("==== JobTask ==== add job: {}", jobName);
    }

    /**
     * 移除任务
     * 等待任务执行完成后移除
     *
     * @param jobId 任务ID
     */
    public static void removeJob(Integer jobId) {
        ScheduledFuture<?> jobFuture = getJobFuture(jobId);
        if (null == jobFuture) {
            return;
        }
        // 结束任务
        stopJob(jobFuture);
        JOB_FUTURE_MAP.remove(jobId);
        log.info("==== JobTask ==== remove job: {}", jobId);
    }

    /**
     * 停止所有定时任务
     */
    public static void destroy() {
        // 启动一个有序的关闭过程,在这个过程中,不再接受新的任务提交,但已提交的任务（包括正在执行的和队列中等待的）会被允许执行完成。
        if (TASK_SCHEDULER != null) {
            TASK_SCHEDULER.destroy();
        }
        if (JOB_FUTURE_MAP != null) {
            JOB_FUTURE_MAP.clear();
        }
        log.info("==== JobTask ==== 所有定时任务已停止");
    }

    /**
     * 结束任务
     * 如果任务还没有开始执行，会直接被取消。
     * 如果任务已经开始执行，此时不会中断执行中的线程，任务会执行完成再被取消
     *
     * @param scheduledFuture 定时任务Future
     */
    private static void stopJob(ScheduledFuture<?> scheduledFuture) {
        if (null == scheduledFuture || scheduledFuture.isCancelled()) {
            return;
        }
        scheduledFuture.cancel(false);
    }
}
