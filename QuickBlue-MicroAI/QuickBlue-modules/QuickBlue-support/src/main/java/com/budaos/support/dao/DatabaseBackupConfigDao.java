package com.budaos.support.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.budaos.support.domain.entity.DatabaseBackupConfigEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

/**
 * 数据库备份配置 DAO
 *
 * @author budaos
 */
@Mapper
public interface DatabaseBackupConfigDao extends BaseMapper<DatabaseBackupConfigEntity> {

}
