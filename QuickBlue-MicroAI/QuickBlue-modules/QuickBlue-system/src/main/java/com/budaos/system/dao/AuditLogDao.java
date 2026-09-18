package com.budaos.system.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.budaos.system.domain.entity.AuditLogEntity;
import com.budaos.system.domain.form.AuditLogQueryForm;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 操作日志 DAO
 *
 * @author budaos
 */
@Mapper
public interface AuditLogDao extends BaseMapper<AuditLogEntity> {

    /**
     * 分页查询
     *
     * @param page      分页对象
     * @param queryForm 查询表单
     * @return 操作日志实体列表
     */
    List<AuditLogEntity> queryByPage(Page page, @Param("query") AuditLogQueryForm queryForm);

    /**
     * 批量删除
     *
     * @param idList 日志ID列表
     */
    void batchDelete(@Param("idList") List<Long> idList);
}
