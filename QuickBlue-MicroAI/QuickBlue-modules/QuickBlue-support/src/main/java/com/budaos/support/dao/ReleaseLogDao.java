package com.budaos.support.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.budaos.support.domain.entity.ReleaseLogEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统更新日志DAO
 *
 * @author budaos
 */
@Mapper
public interface ReleaseLogDao extends BaseMapper<ReleaseLogEntity> {

    /**
     * 分页查询更新日志
     */
    List<com.budaos.support.domain.vo.ReleaseLogVO> query(@Param("page") Object page, @Param("query") com.budaos.support.domain.form.ReleaseLogQueryForm queryForm);
}
