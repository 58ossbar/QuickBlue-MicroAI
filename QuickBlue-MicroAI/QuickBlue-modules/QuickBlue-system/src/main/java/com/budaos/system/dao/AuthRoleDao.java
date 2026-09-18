package com.budaos.system.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.budaos.system.domain.entity.AuthRoleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 角色 DAO
 *
 * @author budaos
 */
@Mapper
public interface AuthRoleDao extends BaseMapper<AuthRoleEntity> {

    /**
     * 根据角色名称查询
     *
     * @param roleName 角色名称
     * @return 角色实体
     */
    AuthRoleEntity getByRoleName(@Param("roleName") String roleName);

    /**
     * 根据角色编码查询
     *
     * @param roleCode 角色编码
     * @return 角色实体
     */
    AuthRoleEntity getByRoleCode(@Param("roleCode") String roleCode);
}
