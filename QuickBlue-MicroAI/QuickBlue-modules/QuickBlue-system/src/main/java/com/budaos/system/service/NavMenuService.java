package com.budaos.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.common.core.domain.RequestUrlInfo;
import com.budaos.system.domain.entity.NavMenuEntity;
import com.budaos.system.domain.form.NavMenuAddForm;
import com.budaos.system.domain.form.NavMenuUpdateForm;
import com.budaos.system.domain.vo.NavMenuTreeVO;
import com.budaos.system.domain.vo.NavMenuVO;

import java.util.List;

/**
 * 菜单服务接口
 *
 * @author budaos
 */
public interface NavMenuService extends IService<NavMenuEntity> {

    /**
     * 添加菜单
     *
     * @param addForm 添加表单
     * @return 操作结果
     */
    ApiResult<String> addMenu(NavMenuAddForm addForm);

    /**
     * 更新菜单
     *
     * @param updateForm 更新表单
     * @return 操作结果
     */
    ApiResult<String> updateMenu(NavMenuUpdateForm updateForm);

    /**
     * 批量删除菜单
     *
     * @param menuIdList 菜单ID列表
     * @param employeeId 操作人ID
     * @return 操作结果
     */
    ApiResult<String> batchDeleteMenu(List<Long> menuIdList, Long employeeId);

    /**
     * 查询菜单列表
     *
     * @param disabledFlag 是否禁用
     * @return 菜单列表
     */
    List<NavMenuVO> queryMenuList(Boolean disabledFlag);

    /**
     * 查询菜单详情
     *
     * @param menuId 菜单ID
     * @return 菜单VO
     */
    ApiResult<NavMenuVO> getMenuDetail(Long menuId);

    /**
     * 查询菜单树
     *
     * @param onlyMenu 是否只查询菜单（不包含功能点）
     * @return 菜单树
     */
    ApiResult<List<NavMenuTreeVO>> queryMenuTree(Boolean onlyMenu);

    /**
     * 获取所有请求路径
     *
     * @return 请求路径列表
     */
    ApiResult<List<RequestUrlInfo>> getAuthUrl();
}
