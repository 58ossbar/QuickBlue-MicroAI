package com.budaos.support.jobtask.repository;

import com.budaos.support.jobtask.dao.JobTaskDao;
import com.budaos.support.jobtask.dao.JobTaskLogDao;
import com.budaos.support.domain.entity.JobTaskEntity;
import com.budaos.support.domain.entity.JobTaskLogEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * job 持久化业务
 *
 */
@Service
public class JobTaskRepository {

    @Autowired
    private JobTaskDao jobDao;

    @Autowired
    private JobTaskLogDao jobLogDao;

    public JobTaskDao getJobDao() {
        return jobDao;
    }

    public JobTaskLogDao getJobLogDao() {
        return jobLogDao;
    }

    /**
     * 保存执行记录
     *
     * @param logEntity
     * @param jobEntity
     */
    @Transactional(rollbackFor = Throwable.class)
    public void saveLog(JobTaskLogEntity logEntity, JobTaskEntity jobEntity) {
        jobLogDao.insert(logEntity);

        jobEntity.setLastExecuteLogId(logEntity.getLogId());
        jobDao.updateById(jobEntity);
    }
}
