package com.budaos.support.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.budaos.support.domain.entity.SystemConfigEntity;
import com.budaos.support.domain.form.SystemConfigQueryForm;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统参数配置 t_config Dao层
 */
@Mapper
public interface SystemConfigDao extends BaseMapper<SystemConfigEntity> {

    /**
     * 分页查询系统配置
     */
    List<SystemConfigEntity> queryByPage(Page page, @Param("query") SystemConfigQueryForm queryForm);

    /**
     * 根据key查询获取数据
     */
    SystemConfigEntity selectByKey(String key);
}
