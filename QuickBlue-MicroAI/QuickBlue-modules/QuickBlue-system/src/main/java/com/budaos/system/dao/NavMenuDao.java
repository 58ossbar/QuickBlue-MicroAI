package com.budaos.system.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.budaos.system.domain.entity.NavMenuEntity;
import com.budaos.system.domain.vo.NavMenuVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 菜单 DAO
 *
 * @author budaos
 */
@Mapper
public interface NavMenuDao extends BaseMapper<NavMenuEntity> {

    /**
     * 根据名称查询同一级下的菜单
     *
     * @param menuName    菜单名
     * @param parentId    父级ID
     * @param deletedFlag 是否删除
     * @return 菜单实体
     */
    NavMenuEntity getByMenuName(@Param("menuName") String menuName, @Param("parentId") Long parentId, @Param("deletedFlag") Boolean deletedFlag);

    /**
     * 根据前端权限字符串查询菜单
     *
     * @param webPerms    前端权限字符串
     * @param deletedFlag 是否删除
     * @return 菜单实体
     */
    NavMenuEntity getByWebPerms(@Param("webPerms") String webPerms, @Param("deletedFlag") Boolean deletedFlag);

    /**
     * 根据菜单ID删除菜单（逻辑删除）
     *
     * @param menuIdList   菜单ID集合
     * @param updateUserId 操作人ID
     * @param deletedFlag  是否删除
     */
    void deleteByMenuIdList(@Param("menuIdList") List<Long> menuIdList, @Param("updateUserId") Long updateUserId, @Param("deletedFlag") Boolean deletedFlag);

    /**
     * 查询菜单列表
     *
     * @param deletedFlag  是否删除
     * @param disabledFlag 是否禁用
     * @param menuTypeList 菜单类型集合
     * @return 菜单VO列表
     */
    List<NavMenuVO> queryMenuList(@Param("deletedFlag") Boolean deletedFlag, @Param("disabledFlag") Boolean disabledFlag, @Param("menuTypeList") List<Integer> menuTypeList);

    /**
     * 根据菜单ID查询功能点列表
     *
     * @param menuId      菜单ID
     * @param menuType    菜单类型
     * @param deletedFlag 删除标记
     * @return 菜单实体列表
     */
    List<NavMenuEntity> getPointListByMenuId(@Param("menuId") Long menuId, @Param("menuType") Integer menuType, @Param("deletedFlag") Boolean deletedFlag);

    /**
     * 根据员工ID查询菜单列表
     *
     * @param deletedFlag  是否删除
     * @param disabledFlag 禁用标识
     * @param employeeId   员工ID
     * @return 菜单VO列表
     */
    List<NavMenuVO> queryMenuByEmployeeId(@Param("deletedFlag") Boolean deletedFlag,
                                         @Param("disabledFlag") Boolean disabledFlag,
                                         @Param("employeeId") Long employeeId);

    /**
     * 根据菜单类型查询
     *
     * @param menuType     菜单类型
     * @param deletedFlag  删除标记
     * @param disabledFlag 禁用标记
     * @return 菜单实体列表
     */
    List<NavMenuEntity> queryMenuByType(@Param("menuType") Integer menuType,
                                      @Param("deletedFlag") Boolean deletedFlag,
                                      @Param("disabledFlag") Boolean disabledFlag);

    /**
     * 根据父菜单ID列表查询子菜单ID列表
     *
     * @param menuIdList 父菜单ID列表
     * @return 子菜单ID列表
     */
    List<Long> selectMenuIdByParentIdList(@Param("menuIdList") List<Long> menuIdList);
}
