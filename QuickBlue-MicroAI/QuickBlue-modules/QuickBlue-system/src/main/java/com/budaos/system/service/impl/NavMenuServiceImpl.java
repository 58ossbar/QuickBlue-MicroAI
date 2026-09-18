package com.budaos.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.budaos.common.core.code.UserErrorCodes;
import com.budaos.common.core.domain.RequestUrlInfo;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.system.constant.NavMenuPermsTypeEnum;
import com.budaos.system.constant.NavMenuTypeEnum;
import com.budaos.system.dao.NavMenuDao;
import com.budaos.system.domain.entity.NavMenuEntity;
import com.budaos.system.domain.form.NavMenuAddForm;
import com.budaos.system.domain.form.NavMenuBaseForm;
import com.budaos.system.domain.form.NavMenuUpdateForm;
import com.budaos.system.domain.vo.NavMenuTreeVO;
import com.budaos.system.domain.vo.NavMenuVO;
import com.budaos.system.service.NavMenuService;
import com.budaos.common.core.util.BeanCopyUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 菜单服务实现
 *
 * @author budaos
 */
@Service
@RequiredArgsConstructor
public class NavMenuServiceImpl extends ServiceImpl<NavMenuDao, NavMenuEntity> implements NavMenuService {

    private final NavMenuDao menuDao;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<String> addMenu(NavMenuAddForm addForm) {
        // 校验菜单名称
        if (validateMenuName(addForm)) {
            return ApiResult.userErrorParam("菜单名称已存在");
        }
        // 校验前端权限字符串
        if (validateWebPerms(addForm)) {
            return ApiResult.userErrorParam("前端权限字符串已存在");
        }
        NavMenuEntity menuEntity = new NavMenuEntity();
        BeanCopyUtil.copyProperties(addForm, menuEntity);
        menuDao.insert(menuEntity);
        return ApiResult.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<String> updateMenu(NavMenuUpdateForm updateForm) {
        // 校验菜单是否存在
        NavMenuEntity selectMenu = menuDao.selectById(updateForm.getMenuId());
        if (selectMenu == null) {
            return ApiResult.userErrorParam("菜单不存在");
        }
        if (selectMenu.getDeletedFlag()) {
            return ApiResult.userErrorParam("菜单已被删除");
        }
        // 校验菜单名称
        if (validateMenuName(updateForm)) {
            return ApiResult.userErrorParam("菜单名称已存在");
        }
        // 校验前端权限字符串
        if (validateWebPerms(updateForm)) {
            return ApiResult.userErrorParam("前端权限字符串已存在");
        }
        if (updateForm.getMenuId().equals(updateForm.getParentId())) {
            return ApiResult.userErrorParam("上级菜单不能为自己");
        }
        NavMenuEntity menuEntity = new NavMenuEntity();
        BeanCopyUtil.copyProperties(updateForm, menuEntity);
        menuDao.updateById(menuEntity);
        return ApiResult.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<String> batchDeleteMenu(List<Long> menuIdList, Long employeeId) {
        if (CollectionUtil.isEmpty(menuIdList)) {
            return ApiResult.userErrorParam("所选菜单不能为空");
        }
        menuDao.deleteByMenuIdList(menuIdList, employeeId, Boolean.TRUE);
        // 子节点也需要删除
        recursiveDeleteChildren(menuIdList, employeeId);
        return ApiResult.ok();
    }

    /**
     * 递归删除子菜单
     */
    private void recursiveDeleteChildren(List<Long> menuIdList, Long employeeId) {
        List<Long> childrenMenuIdList = menuDao.selectMenuIdByParentIdList(menuIdList);
        if (CollectionUtil.isEmpty(childrenMenuIdList)) {
            return;
        }
        menuDao.deleteByMenuIdList(childrenMenuIdList, employeeId, Boolean.TRUE);
        recursiveDeleteChildren(childrenMenuIdList, employeeId);
    }

    /**
     * 校验菜单名称
     *
     * @param menuDTO 菜单表单
     * @return true-重复 false-未重复
     */
    private <T extends NavMenuBaseForm> Boolean validateMenuName(T menuDTO) {
        NavMenuEntity menu = menuDao.getByMenuName(menuDTO.getMenuName(), menuDTO.getParentId(), Boolean.FALSE);
        if (menuDTO instanceof NavMenuAddForm) {
            return menu != null;
        }
        if (menuDTO instanceof NavMenuUpdateForm) {
            Long menuId = ((NavMenuUpdateForm) menuDTO).getMenuId();
            return menu != null && !menu.getMenuId().equals(menuId);
        }
        return true;
    }

    /**
     * 校验前端权限字符串
     *
     * @param menuDTO 菜单表单
     * @return true-重复 false-未重复
     */
    private <T extends NavMenuBaseForm> Boolean validateWebPerms(T menuDTO) {
        if (StrUtil.isEmpty(menuDTO.getWebPerms())) {
            return false;
        }

        NavMenuEntity menu = menuDao.getByWebPerms(menuDTO.getWebPerms(), Boolean.FALSE);
        if (menuDTO instanceof NavMenuAddForm) {
            return menu != null;
        }
        if (menuDTO instanceof NavMenuUpdateForm) {
            Long menuId = ((NavMenuUpdateForm) menuDTO).getMenuId();
            return menu != null && !menu.getMenuId().equals(menuId);
        }
        return true;
    }

    @Override
    public List<NavMenuVO> queryMenuList(Boolean disabledFlag) {
        List<NavMenuVO> menuVOList = menuDao.queryMenuList(Boolean.FALSE, disabledFlag, null);
        // 根据ParentId进行分组
        Map<Long, List<NavMenuVO>> parentMap = menuVOList.stream()
                .collect(Collectors.groupingBy(NavMenuVO::getParentId, Collectors.toList()));
        return filterNoParentMenu(parentMap, NumberUtils.LONG_ZERO);
    }

    /**
     * 过滤没有上级菜单的菜单列表
     */
    private List<NavMenuVO> filterNoParentMenu(Map<Long, List<NavMenuVO>> parentMap, Long parentId) {
        // 获取本级菜单树List
        List<NavMenuVO> res = parentMap.getOrDefault(parentId, new ArrayList<>());
        List<NavMenuVO> childMenu = new ArrayList<>();
        // 循环遍历下级菜单
        res.forEach(e -> {
            List<NavMenuVO> menuList = this.filterNoParentMenu(parentMap, e.getMenuId());
            childMenu.addAll(menuList);
        });
        res.addAll(childMenu);
        return res;
    }

    @Override
    public ApiResult<List<NavMenuTreeVO>> queryMenuTree(Boolean onlyMenu) {
        List<Integer> menuTypeList = new ArrayList<>();
        if (onlyMenu) {
            menuTypeList = new ArrayList<>(
                    List.of(
                            NavMenuTypeEnum.CATALOG.getValue(),
                            NavMenuTypeEnum.MENU.getValue()
                    )
            );
        }
        List<NavMenuVO> menuVOList = menuDao.queryMenuList(Boolean.FALSE, null, menuTypeList);
        // 根据ParentId进行分组
        Map<Long, List<NavMenuVO>> parentMap = menuVOList.stream()
                .collect(Collectors.groupingBy(NavMenuVO::getParentId, Collectors.toList()));
        List<NavMenuTreeVO> menuTreeList = buildMenuTree(parentMap, NumberUtils.LONG_ZERO);
        return ApiResult.ok(menuTreeList);
    }

    /**
     * 构建菜单树
     */
    private List<NavMenuTreeVO> buildMenuTree(Map<Long, List<NavMenuVO>> parentMap, Long parentId) {
        // 获取本级菜单树List
        List<NavMenuTreeVO> res = parentMap.getOrDefault(parentId, new ArrayList<>()).stream()
                .map(e -> {
                    NavMenuTreeVO treeVO = new NavMenuTreeVO();
                    BeanCopyUtil.copyProperties(e, treeVO);
                    return treeVO;
                }).collect(Collectors.toList());
        // 循环遍历下级菜单
        res.forEach(e -> {
            e.setChildren(this.buildMenuTree(parentMap, e.getMenuId()));
        });
        return res;
    }

    @Override
    public ApiResult<NavMenuVO> getMenuDetail(Long menuId) {
        // 校验菜单是否存在
        NavMenuEntity selectMenu = menuDao.selectById(menuId);
        if (selectMenu == null) {
            return ApiResult.error(UserErrorCodes.DATA_NOT_EXIST);
        }
        if (selectMenu.getDeletedFlag()) {
            return ApiResult.error(UserErrorCodes.DATA_DELETED);
        }
        NavMenuVO menuVO = new NavMenuVO();
        BeanCopyUtil.copyProperties(selectMenu, menuVO);
        return ApiResult.ok(menuVO);
    }

    @Override
    public ApiResult<List<RequestUrlInfo>> getAuthUrl() {
        // TODO: 从Spring容器中获取RequestUrlVO列表
        // 暂时返回空列表，后续可以通过@PostConstruct或其他方式注入
        return ApiResult.ok(new ArrayList<>());
    }
}
