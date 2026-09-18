package com.budaos.support.jobtask.core;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.budaos.support.util.JobTaskUtil;
import com.budaos.support.jobtask.repository.JobTaskRepository;
import com.budaos.support.domain.entity.JobTaskEntity;
import com.budaos.support.domain.entity.JobTaskLogEntity;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.util.StopWatch;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

/**
 * 定时任务 执行器
 * 负责执行具体的定时任务逻辑
 *
 * @author budaos
 */
@Slf4j
public class JobTaskExecutor implements Runnable {

    private final JobTaskEntity jobEntity;

    private final JobTaskRepository jobRepository;

    private final JobTask jobInterface;

    private final RedissonClient redissonClient;

    private static final String EXECUTE_LOCK = "quickblue-job-lock-execute-";

    public JobTaskExecutor(JobTaskEntity jobEntity,
                            JobTaskRepository jobRepository,
                            JobTask jobInterface,
                            RedissonClient redissonClient) {
        this.jobEntity = jobEntity;
        this.jobRepository = jobRepository;
        this.jobInterface = jobInterface;
        this.redissonClient = redissonClient;
    }

    /**
     * 系统线程执行
     */
    @Override
    public void run() {
        // 获取当前任务执行锁 最多持有30s自动释放
        Integer jobId = jobEntity.getJobId();
        RLock rLock = redissonClient.getLock(EXECUTE_LOCK + jobId);
        try {
            boolean lock = rLock.tryLock(0, 30, TimeUnit.SECONDS);
            if (!lock) {
                return;
            }
            // 查询上次执行时间 校验执行间隔
            JobTaskEntity dbJobEntity = jobRepository.getJobDao().selectById(jobId);
            if (null == dbJobEntity) {
                return;
            }
            LocalDateTime lastExecuteTime = dbJobEntity.getLastExecuteTime();
            if (null != lastExecuteTime) {
                LocalDateTime nextTime = JobTaskUtil.queryNextTimeFromLast(jobEntity.getTriggerType(), jobEntity.getTriggerValue(), lastExecuteTime, 1).get(0);
                if (LocalDateTime.now().isBefore(nextTime)) {
                    return;
                }
            }
            // 执行任务
            JobTaskLogEntity logEntity = this.execute("SYSTEM");
            log.info("==== JobTask ==== execute job->{},time-millis->{}ms", jobEntity.getJobName(), logEntity.getExecuteTimeMillis());
        } catch (Throwable t) {
            log.error("==== JobTask ==== execute err:", t);
        } finally {
            if (rLock.isHeldByCurrentThread()) {
                rLock.unlock();
            }
        }
    }

    /**
     * 执行任务
     *
     * @param executorName 执行器名称
     * @return 执行日志
     */
    public JobTaskLogEntity execute(String executorName) {
        // 保存执行记录
        LocalDateTime startTime = LocalDateTime.now();
        Long logId = this.saveLogBeforeExecute(jobEntity, executorName, startTime);

        // 执行计时
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // 执行任务
        boolean successFlag = true;
        String executeResult;
        try {
            executeResult = jobInterface.run(jobEntity.getParam());
            stopWatch.stop();
        } catch (Throwable t) {
            stopWatch.stop();
            successFlag = false;
            // ps:异常信息不大于数据库字段长度限制
            executeResult = ExceptionUtil.stacktraceToString(t, 1800);
            log.error("==== JobTask ==== execute err:", t);
        }

        // 更新执行记录
        JobTaskLogEntity logEntity = new JobTaskLogEntity();
        logEntity.setLogId(logId);
        logEntity.setSuccessFlag(successFlag);
        long totalTimeMillis = stopWatch.getTotalTimeMillis();
        logEntity.setExecuteTimeMillis(totalTimeMillis);
        logEntity.setExecuteEndTime(startTime.plus(totalTimeMillis, ChronoUnit.MILLIS));
        logEntity.setExecuteResult(executeResult);
        jobRepository.getJobLogDao().updateById(logEntity);
        return logEntity;
    }

    /**
     * 执行前 保存执行记录
     *
     * @param jobEntity     任务实体
     * @param executorName  执行器名称
     * @param executeTime   执行时间
     * @return 返回执行记录id
     */
    private Long saveLogBeforeExecute(JobTaskEntity jobEntity,
                                      String executorName,
                                      LocalDateTime executeTime) {
        Integer jobId = jobEntity.getJobId();
        // 保存执行记录
        JobTaskLogEntity logEntity = new JobTaskLogEntity();
        logEntity.setJobId(jobId);
        logEntity.setJobName(jobEntity.getJobName());
        logEntity.setParam(jobEntity.getParam());
        logEntity.setSuccessFlag(true);
        // 执行开始时间
        logEntity.setExecuteStartTime(executeTime);
        logEntity.setExecuteEndTime(executeTime);
        logEntity.setExecuteTimeMillis(0L);
        logEntity.setCreateName(executorName);
        logEntity.setIp(getLocalIp());
        logEntity.setProcessId(JobTaskUtil.getProcessId());
        logEntity.setProgramPath(JobTaskUtil.getProgramPath());

        // 更新最后执行时间
        JobTaskEntity updateJobEntity = new JobTaskEntity();
        updateJobEntity.setJobId(jobId);
        updateJobEntity.setLastExecuteTime(executeTime);
        jobRepository.saveLog(logEntity, updateJobEntity);
        return logEntity.getLogId();
    }

    /**
     * 获取本地IP
     *
     * @return IP地址
     */
    private String getLocalIp() {
        try {
            return java.net.InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "unknown";
        }
    }

    /**
     * 查询 当前任务信息
     *
     * @return 任务实体
     */
    public JobTaskEntity getJob() {
        return jobEntity;
    }
}
