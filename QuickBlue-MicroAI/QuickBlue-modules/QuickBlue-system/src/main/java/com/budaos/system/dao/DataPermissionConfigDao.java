package com.budaos.system.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.budaos.system.domain.entity.DataPermissionConfigEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 数据权限配置 DAO
 *
 * @author budaos
 */
@Mapper
public interface DataPermissionConfigDao extends BaseMapper<DataPermissionConfigEntity> {

    /**
     * 查询所有启用的数据权限配置（按排序排序）
     *
     * @return 数据权限配置列表
     */
    default List<DataPermissionConfigEntity> selectAllEnabled() {
        return selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DataPermissionConfigEntity>()
                .eq(DataPermissionConfigEntity::getStatus, true)
                .orderByAsc(DataPermissionConfigEntity::getSortOrder)
        );
    }

    /**
     * 根据配置编码查询数据权限配置
     *
     * @param configCode 配置编码
     * @return 数据权限配置
     */
    default DataPermissionConfigEntity selectByConfigCode(String configCode) {
        return selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DataPermissionConfigEntity>()
                .eq(DataPermissionConfigEntity::getConfigCode, configCode)
                .eq(DataPermissionConfigEntity::getStatus, true)
        );
    }
}
