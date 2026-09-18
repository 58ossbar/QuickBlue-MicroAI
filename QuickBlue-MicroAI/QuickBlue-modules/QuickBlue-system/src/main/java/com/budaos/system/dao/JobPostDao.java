package com.budaos.system.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.budaos.system.domain.entity.JobPostEntity;
import com.budaos.system.domain.form.JobPostQueryForm;
import com.budaos.system.domain.vo.JobPostVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 职务表DAO
 *
 * @author budaos
 */
@Mapper
public interface JobPostDao extends BaseMapper<JobPostEntity> {

    /**
     * 分页查询
     *
     * @param page      分页对象
     * @param queryForm 查询表单
     * @return 职务VO列表
     */
    List<JobPostVO> queryPage(Page page, @Param("queryForm") JobPostQueryForm queryForm);

    /**
     * 查询所有职务
     *
     * @param deletedFlag 删除标记
     * @return 职务VO列表
     */
    List<JobPostVO> queryList(@Param("deletedFlag") Boolean deletedFlag);

}
