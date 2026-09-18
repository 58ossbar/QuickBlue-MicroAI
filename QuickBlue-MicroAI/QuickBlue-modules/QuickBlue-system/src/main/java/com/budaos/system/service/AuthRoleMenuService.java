package com.budaos.system.service;

import com.budaos.common.core.domain.ApiResult;
import com.budaos.common.core.code.UserErrorCodes;
import com.budaos.common.core.util.BeanCopyUtil;
import com.budaos.system.dao.NavMenuDao;
import com.budaos.system.dao.AuthRoleDao;
import com.budaos.system.dao.AuthRoleMenuDao;
import com.budaos.system.domain.entity.NavMenuEntity;
import com.budaos.system.domain.entity.AuthRoleEntity;
import com.budaos.system.domain.entity.AuthRoleMenuEntity;
import com.budaos.system.domain.form.AuthRoleMenuUpdateForm;
import com.budaos.system.domain.vo.NavMenuSimpleTreeVO;
import com.budaos.system.domain.vo.NavMenuVO;
import com.budaos.system.domain.vo.AuthRoleMenuTreeVO;
import com.budaos.system.manager.AuthRoleMenuManager;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 角色菜单服务
 *
 * @author budaos
 */
@Service
public class AuthRoleMenuService {

    @Resource
    private AuthRoleDao roleDao;

    @Resource
    private AuthRoleMenuDao roleMenuDao;

    @Resource
    private AuthRoleMenuManager roleMenuManager;

    @Resource
    private NavMenuDao menuDao;

    /**
     * 更新角色菜单权限
     *
     * @param roleMenuUpdateForm 角色菜单更新表单
     * @return 响应
     */
    public ApiResult<String> updateRoleMenu(AuthRoleMenuUpdateForm roleMenuUpdateForm) {
        // 查询角色是否存在
        Long roleId = roleMenuUpdateForm.getRoleId();
        AuthRoleEntity roleEntity = roleDao.selectById(roleId);
        if (null == roleEntity) {
            return ApiResult.error(UserErrorCodes.DATA_NOT_EXIST);
        }

        List<AuthRoleMenuEntity> roleMenuEntityList = new ArrayList<>();
        AuthRoleMenuEntity roleMenuEntity;
        for (Long menuId : roleMenuUpdateForm.getMenuIdList()) {
            roleMenuEntity = new AuthRoleMenuEntity();
            roleMenuEntity.setRoleId(roleId);
            roleMenuEntity.setMenuId(menuId);
            roleMenuEntityList.add(roleMenuEntity);
        }

        roleMenuManager.updateRoleMenu(roleMenuUpdateForm.getRoleId(), roleMenuEntityList);
        return ApiResult.ok();
    }

    /**
     * 根据角色ID集合，查询其所有的菜单权限
     *
     * @param roleIdList 角色ID列表
     * @param administratorFlag 是否管理员
     * @return 菜单VO列表
     */
    public List<NavMenuVO> getMenuList(List<Long> roleIdList, Boolean administratorFlag) {
        // 管理员返回所有菜单
        if (administratorFlag) {
            List<NavMenuEntity> menuEntityList = roleMenuDao.selectMenuListByRoleIdList(new ArrayList<>(), false);
            return BeanCopyUtil.copyList(menuEntityList, NavMenuVO.class);
        }

        // 非管理员无角色返回空菜单
        if (CollectionUtils.isEmpty(roleIdList)) {
            return new ArrayList<>();
        }

        List<NavMenuEntity> menuEntityList = roleMenuDao.selectMenuListByRoleIdList(roleIdList, false);
        return BeanCopyUtil.copyList(menuEntityList, NavMenuVO.class);
    }

    /**
     * 获取角色关联菜单权限
     *
     * @param roleId 角色ID
     * @return 响应
     */
    public ApiResult<AuthRoleMenuTreeVO> getRoleSelectedMenu(Long roleId) {
        AuthRoleMenuTreeVO res = new AuthRoleMenuTreeVO();
        res.setRoleId(roleId);

        // 查询角色ID选择的菜单权限
        List<Long> selectedMenuId = roleMenuDao.queryMenuIdByRoleId(roleId);
        res.setSelectedMenuId(selectedMenuId);

        // 查询菜单权限
        List<NavMenuVO> menuVOList = menuDao.queryMenuList(Boolean.FALSE, Boolean.FALSE, null);
        Map<Long, List<NavMenuVO>> parentMap = menuVOList.stream()
                .collect(Collectors.groupingBy(NavMenuVO::getParentId, Collectors.toList()));
        List<NavMenuSimpleTreeVO> menuTreeList = this.buildMenuTree(parentMap, 0L);
        res.setMenuTreeList(menuTreeList);

        return ApiResult.ok(res);
    }

    /**
     * 构建菜单树
     *
     * @param parentMap 父级菜单Map
     * @param parentId 父级ID
     * @return 菜单树
     */
    private List<NavMenuSimpleTreeVO> buildMenuTree(Map<Long, List<NavMenuVO>> parentMap, Long parentId) {
        // 获取本级菜单树List
        List<NavMenuSimpleTreeVO> res = parentMap.getOrDefault(parentId, new ArrayList<>()).stream()
                .map(e -> BeanCopyUtil.copy(e, NavMenuSimpleTreeVO.class))
                .collect(Collectors.toList());
        // 循环遍历下级菜单
        res.forEach(e -> e.setChildren(this.buildMenuTree(parentMap, e.getMenuId())));
        return res;
    }

}
