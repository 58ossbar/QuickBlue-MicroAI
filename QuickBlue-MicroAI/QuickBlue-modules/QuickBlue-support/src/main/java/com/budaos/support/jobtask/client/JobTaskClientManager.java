package com.budaos.support.jobtask.client;

import cn.hutool.core.util.IdUtil;
import com.budaos.support.domain.entity.JobTaskEntity;
import com.budaos.support.domain.vo.JobTaskMessage;
import com.budaos.support.jobtask.config.JobTaskAutoConfiguration;
import com.budaos.support.jobtask.core.JobTask;
import com.budaos.support.jobtask.core.JobTaskExecutor;
import com.budaos.support.jobtask.core.JobTaskLauncher;
import com.budaos.support.jobtask.repository.JobTaskRepository;
import com.google.common.collect.Lists;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * job task 执行端管理
 * 分布式系统之间 用发布/订阅消息的形式 来管理多个job
 *
 * @author budaos
 */
@ConditionalOnBean(JobTaskAutoConfiguration.class)
@Slf4j
@Service
public class JobTaskClientManager {

    private final JobTaskLauncher jobLauncher;

    private final JobTaskRepository jobRepository;

    private final List<JobTask> jobInterfaceList;

    private static final String EXECUTE_LOCK = "quickblue-job-lock-msg-execute-";

    private static final String TOPIC = "quickblue-job-instance";

    private final RedissonClient redissonClient;

    private final RTopic topic;

    private final JobTaskMsgListener jobMsgListener;

    public JobTaskClientManager(JobTaskLauncher jobLauncher,
                                 JobTaskRepository jobRepository,
                                 List<JobTask> jobInterfaceList,
                                 RedissonClient redissonClient) {
        this.jobLauncher = jobLauncher;
        this.jobRepository = jobRepository;
        this.jobInterfaceList = jobInterfaceList;
        this.redissonClient = redissonClient;

        // 添加监听器
        this.topic = redissonClient.getTopic(TOPIC);
        this.jobMsgListener = new JobTaskMsgListener();
        topic.addListener(JobTaskMessage.class, jobMsgListener);
        log.info("==== JobTask ==== client-manager init");
    }

    /**
     * 发布消息
     *
     * @param msgDTO 消息对象
     */
    public void publishToClient(JobTaskMessage msgDTO) {
        msgDTO.setMsgId(IdUtil.fastSimpleUUID());
        topic.publish(msgDTO);
    }

    /**
     * 处理消息
     */
    private class JobTaskMsgListener implements MessageListener<JobTaskMessage> {

        @Override
        public void onMessage(CharSequence channel, JobTaskMessage msg) {
            log.info("==== JobTask ==== on-message :{}", msg);
            // 判断消息类型 业务简单就直接判断 复杂的话可以策略模式
            JobTaskMessage.MsgTypeEnum msgType = msg.getMsgType();
            // 更新任务
            if (JobTaskMessage.MsgTypeEnum.UPDATE_JOB == msgType) {
                updateJob(msg.getJobId());
            }
            // 执行任务
            if (JobTaskMessage.MsgTypeEnum.EXECUTE_JOB == msgType) {
                executeJob(msg);
            }
        }
    }

    /**
     * 获取任务执行类
     *
     * @param jobClass 任务类名
     * @return Optional<JobTask>
     */
    private Optional<JobTask> queryJobImpl(String jobClass) {
        return jobInterfaceList.stream().filter(e -> Objects.equals(e.getClassName(), jobClass)).findFirst();
    }

    /**
     * 更新任务
     *
     * @param jobId 任务ID
     */
    private void updateJob(Integer jobId) {
        JobTaskEntity jobEntity = jobRepository.getJobDao().selectById(jobId);
        if (null == jobEntity) {
            return;
        }
        jobLauncher.startOrRefreshJob(Lists.newArrayList(jobEntity));
    }

    /**
     * 立即执行任务
     *
     * @param msg 消息对象
     */
    private void executeJob(JobTaskMessage msg) {
        Integer jobId = msg.getJobId();
        JobTaskEntity jobEntity = jobRepository.getJobDao().selectById(jobId);
        if (null == jobEntity) {
            return;
        }
        // 获取定时任务实现类
        Optional<JobTask> optional = this.queryJobImpl(jobEntity.getJobClass());
        if (!optional.isPresent()) {
            return;
        }

        // 获取执行锁 无需主动释放
        RLock rLock = redissonClient.getLock(EXECUTE_LOCK + msg.getMsgId());
        try {
            boolean getLock = rLock.tryLock(0, 20, TimeUnit.SECONDS);
            if (!getLock) {
                return;
            }
        } catch (InterruptedException e) {
            log.error("==== JobTask ==== msg execute err:", e);
            return;
        }

        // 通过执行器 执行任务
        jobEntity.setParam(msg.getParam());
        JobTaskExecutor jobExecutor = new JobTaskExecutor(jobEntity, jobRepository, optional.get(), redissonClient);
        jobExecutor.execute(msg.getUpdateName());
    }


    @PreDestroy
    public void destroy() {
        topic.removeListener(jobMsgListener);
    }


}
