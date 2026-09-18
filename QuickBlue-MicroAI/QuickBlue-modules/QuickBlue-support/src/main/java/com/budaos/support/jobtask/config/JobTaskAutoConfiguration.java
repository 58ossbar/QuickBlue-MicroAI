package com.budaos.support.jobtask.config;

import com.budaos.support.jobtask.core.JobTask;
import com.budaos.support.jobtask.core.JobTaskLauncher;
import com.budaos.support.jobtask.repository.JobTaskRepository;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 定时任务 配置
 *
 */
@Configuration
@EnableConfigurationProperties(JobTaskConfig.class)
@ConditionalOnProperty(
        prefix = JobTaskConfig.CONFIG_PREFIX,
        name = "enabled",
        havingValue = "true"
)
public class JobTaskAutoConfiguration {

    private final JobTaskConfig jobConfig;
    private final JobTaskRepository jobRepository;
    private final List<JobTask> jobInterfaceList;

    public JobTaskAutoConfiguration(JobTaskConfig jobConfig,
                                      JobTaskRepository jobRepository,
                                      List<JobTask> jobInterfaceList) {
        this.jobConfig = jobConfig;
        this.jobRepository = jobRepository;
        this.jobInterfaceList = jobInterfaceList;
    }

    /**
     * 定时任务启动器
     *
     * @return
     */
    @Bean
    public JobTaskLauncher initJobLauncher(RedissonClient redissonClient) {
        return new JobTaskLauncher(jobConfig, jobRepository, jobInterfaceList, redissonClient);
    }
}
