package com.budaos.support.jobtask.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.budaos.support.domain.entity.JobTaskLogEntity;
import com.budaos.support.domain.form.JobTaskLogQueryForm;
import com.budaos.support.domain.vo.JobTaskLogVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 定时任务-执行记录 dao
 *
 */
@Mapper
public interface JobTaskLogDao extends BaseMapper<JobTaskLogEntity> {

    /**
     * 定时任务-执行记录-分页查询
     *
     * @param page
     * @param queryForm
     * @return
     */
    List<JobTaskLogVO> query(Page<?> page, @Param("query") JobTaskLogQueryForm queryForm);
}
