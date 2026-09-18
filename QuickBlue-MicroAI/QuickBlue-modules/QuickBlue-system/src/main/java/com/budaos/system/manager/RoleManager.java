package com.budaos.system.manager;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.budaos.system.dao.AuthRoleStaffDao;
import com.budaos.system.dao.AuthRoleMenuDao;
import com.budaos.system.domain.entity.AuthRoleStaffEntity;
import com.budaos.system.domain.entity.AuthRoleMenuEntity;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 角色管理器（处理事务）
 *
 * @author budaos
 */
@Component
public class RoleManager {

    @Resource
    private AuthRoleMenuDao roleMenuDao;

    @Resource
    private AuthRoleStaffDao roleEmployeeDao;

    /**
     * 保存角色菜单关联
     *
     * @param roleId 角色ID
     * @param menuIdList 菜单ID列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveRoleMenu(Long roleId, List<Long> menuIdList) {
        if (menuIdList == null || menuIdList.isEmpty()) {
            return;
        }

        List<AuthRoleMenuEntity> roleMenuList = new ArrayList<>();
        for (Long menuId : menuIdList) {
            AuthRoleMenuEntity roleMenu = new AuthRoleMenuEntity();
            roleMenu.setRoleId(roleId);
            roleMenu.setMenuId(menuId);
            roleMenuList.add(roleMenu);
        }

        for (AuthRoleMenuEntity roleMenu : roleMenuList) {
            roleMenuDao.insert(roleMenu);
        }
    }

    /**
     * 保存角色员工关联
     *
     * @param roleId 角色ID
     * @param employeeIdList 员工ID列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveRoleEmployee(Long roleId, List<Long> employeeIdList) {
        if (employeeIdList == null || employeeIdList.isEmpty()) {
            return;
        }

        List<AuthRoleStaffEntity> roleEmployeeList = new ArrayList<>();
        for (Long employeeId : employeeIdList) {
            AuthRoleStaffEntity roleEmployee = new AuthRoleStaffEntity();
            roleEmployee.setRoleId(roleId);
            roleEmployee.setEmployeeId(employeeId);
            roleEmployeeList.add(roleEmployee);
        }

        for (AuthRoleStaffEntity roleEmployee : roleEmployeeList) {
            roleEmployeeDao.insert(roleEmployee);
        }
    }

    /**
     * 更新角色菜单关联
     *
     * @param roleId 角色ID
     * @param menuIdList 菜单ID列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateRoleMenu(Long roleId, List<Long> menuIdList) {
        // 删除旧的菜单关联
        roleMenuDao.deleteByRoleId(roleId);

        // 保存新的菜单关联
        saveRoleMenu(roleId, menuIdList);
    }

    /**
     * 批量删除角色
     *
     * @param roleIdList 角色ID列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteRole(List<Long> roleIdList) {
        if (roleIdList == null || roleIdList.isEmpty()) {
            return;
        }

        for (Long roleId : roleIdList) {
            // 删除菜单关联
            roleMenuDao.deleteByRoleId(roleId);

            // 删除员工关联
            roleEmployeeDao.deleteByRoleId(roleId);
        }
    }
}
