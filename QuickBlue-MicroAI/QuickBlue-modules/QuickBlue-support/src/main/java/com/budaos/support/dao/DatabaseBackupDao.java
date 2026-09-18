package com.budaos.support.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.budaos.support.domain.entity.DatabaseBackupEntity;
import com.budaos.support.domain.form.DatabaseBackupQueryForm;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 数据库备份 DAO
 *
 * @author budaos
 */
@Mapper
public interface DatabaseBackupDao extends BaseMapper<DatabaseBackupEntity> {

    /**
     * 分页查询备份记录
     */
    List<DatabaseBackupEntity> queryByPage(Page page, @Param("query") DatabaseBackupQueryForm queryForm);
}
