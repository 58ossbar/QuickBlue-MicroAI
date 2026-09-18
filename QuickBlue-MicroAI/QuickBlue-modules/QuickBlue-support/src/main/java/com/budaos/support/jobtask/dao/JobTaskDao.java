package com.budaos.support.jobtask.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.budaos.support.domain.entity.JobTaskEntity;
import com.budaos.support.domain.form.JobTaskQueryForm;
import com.budaos.support.domain.vo.JobTaskVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 定时任务 dao
 *
 */
@Mapper
public interface JobTaskDao extends BaseMapper<JobTaskEntity> {

    /**
     * 定时任务-分页查询
     *
     * @param page
     * @param queryForm
     * @return
     */
    List<JobTaskVO> query(Page<?> page, @Param("query") JobTaskQueryForm queryForm);

    /**
     * 假删除
     *
     * @param jobId
     * @return
     */
    void updateDeletedFlag(@Param("jobId") Integer jobId, @Param("deletedFlag") Boolean deletedFlag);

    /**
     * 根据 任务class 查找
     *
     * @param jobClass
     * @return
     */
    JobTaskEntity selectByJobClass(@Param("jobClass") String jobClass);

    /**
     * 查询所有启用的任务
     *
     * @return
     */
    List<JobTaskEntity> selectEnabledList();
}
