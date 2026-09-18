package com.budaos.system.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.budaos.system.domain.entity.AuthRoleDataPermissionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色数据权限DAO
 *
 * @author budaos
 */
@Mapper
public interface AuthRoleDataPermissionDao extends BaseMapper<AuthRoleDataPermissionEntity> {

    /**
     * 获取某个角色的设置信息
     *
     * @param roleId 角色ID
     * @return 角色数据权限列表
     */
    List<AuthRoleDataPermissionEntity> listByRoleId(@Param("roleId") Long roleId);

    /**
     * 获取某批角色的所有数据范围配置信息
     *
     * @param roleIdList 角色ID列表
     * @return 角色数据权限列表
     */
    List<AuthRoleDataPermissionEntity> listByRoleIdList(@Param("roleIdList") List<Long> roleIdList);

    /**
     * 删除某个角色的设置信息
     *
     * @param roleId 角色ID
     */
    void deleteByRoleId(@Param("roleId") Long roleId);
}
