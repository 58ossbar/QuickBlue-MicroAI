package com.budaos.support.nacos.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.budaos.support.nacos.domain.entity.SysConfigAuditEntity;
import com.budaos.support.nacos.domain.form.ConfigAuditQueryForm;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 配置审计DAO
 *
 * @author budaos
 * @since 2026-02-24
 */
@Mapper
public interface SysConfigAuditDao extends BaseMapper<SysConfigAuditEntity> {

    /**
     * 分页查询配置审计记录
     *
     * @param page      分页参数
     * @param queryForm 查询条件
     * @return 审计记录列表
     */
    List<SysConfigAuditEntity> queryByPage(Page<SysConfigAuditEntity> page, @Param("query") ConfigAuditQueryForm queryForm);
}
