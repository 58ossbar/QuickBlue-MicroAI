package com.budaos.support.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.budaos.support.domain.entity.KeepAliveRecordEntity;
import com.budaos.support.domain.form.KeepAliveRecordQueryForm;
import com.budaos.support.domain.vo.KeepAliveRecordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 心跳记录 DAO
 *
 * @author budaos
 */
@Mapper
public interface KeepAliveRecordDao extends BaseMapper<KeepAliveRecordEntity> {

    /**
     * 更新心跳日志
     *
     * @param id             记录ID
     * @param heartBeatTime  心跳时间
     */
    void updateHeartBeatTimeById(@Param("id") Long id, @Param("heartBeatTime") LocalDateTime heartBeatTime);

    /**
     * 查询心跳日志
     *
     * @param heartBeatRecordEntity 心跳记录实体
     * @return 心跳记录实体
     */
    KeepAliveRecordEntity query(KeepAliveRecordEntity heartBeatRecordEntity);

    /**
     * 分页查询
     *
     * @param page                  分页对象
     * @param heartBeatRecordQueryForm 查询表单
     * @return 心跳记录VO列表
     */
    List<KeepAliveRecordVO> pageQuery(Page page, @Param("query") KeepAliveRecordQueryForm heartBeatRecordQueryForm);
}
