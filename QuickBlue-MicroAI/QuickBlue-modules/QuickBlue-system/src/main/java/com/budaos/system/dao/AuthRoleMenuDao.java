package com.budaos.system.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.budaos.system.domain.entity.NavMenuEntity;
import com.budaos.system.domain.entity.AuthRoleMenuEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色菜单DAO
 *
 * @author budaos
 */
@Mapper
public interface AuthRoleMenuDao extends BaseMapper<AuthRoleMenuEntity> {

    /**
     * 根据角色ID删除菜单权限
     *
     * @param roleId 角色ID
     */
    void deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据角色ID查询选择的菜单权限
     *
     * @param roleId 角色ID
     * @return 菜单ID列表
     */
    List<Long> queryMenuIdByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据角色ID集合查询选择的菜单权限
     *
     * @param roleIdList 角色ID列表
     * @param deletedFlag 删除标记
     * @return 菜单实体列表
     */
    List<NavMenuEntity> selectMenuListByRoleIdList(@Param("roleIdList") List<Long> roleIdList, @Param("deletedFlag") Boolean deletedFlag);

    /**
     * 查询所有的角色菜单
     *
     * @return 角色菜单实体列表
     */
    List<AuthRoleMenuEntity> queryAllRoleMenu();

}
