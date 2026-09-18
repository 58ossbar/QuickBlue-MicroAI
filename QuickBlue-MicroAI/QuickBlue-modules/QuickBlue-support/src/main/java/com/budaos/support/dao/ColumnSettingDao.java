package com.budaos.support.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.budaos.support.domain.entity.ColumnSettingEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 表格自定义列（前端用户自定义表格列，并保存到数据库里）
 */
@Mapper
public interface ColumnSettingDao extends BaseMapper<ColumnSettingEntity> {

    ColumnSettingEntity selectByUserIdAndTableId(@Param("userId") Long userId, @Param("userType") Integer userType, @Param("tableId") Integer tableId);

    void deleteTableColumn(@Param("userId") Long userId, @Param("userType") Integer userType, @Param("tableId") Integer tableId);
}
