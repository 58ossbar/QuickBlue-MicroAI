package com.budaos.support.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.budaos.support.domain.entity.DataChangeTraceEntity;
import com.budaos.support.domain.form.DataChangeTraceQueryForm;
import com.budaos.support.domain.vo.DataChangeTraceVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 数据变动记录 Dao
 *
 * @author QuickBlue
 * @date 2026-02-08
 */
@Mapper
public interface DataChangeTraceDao extends BaseMapper<DataChangeTraceEntity> {

    /**
     * 操作记录查询
     */
    List<DataChangeTraceVO> selectRecord(@Param("dataId") Long dataId, @Param("dataType") Integer dataType);

    /**
     * 分页查询
     */
    List<DataChangeTraceVO> query(Page page, @Param("query") DataChangeTraceQueryForm queryForm);
}
