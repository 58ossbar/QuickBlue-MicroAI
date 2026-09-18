package com.budaos.system.manager;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.budaos.system.dao.StaffDao;
import com.budaos.system.dao.AuthRoleStaffDao;
import com.budaos.system.domain.entity.StaffEntity;
import com.budaos.system.domain.entity.AuthRoleStaffEntity;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 员工管理器（处理事务）
 *
 * @author budaos
 */
@Component
public class StaffManager {

    @Resource
    private StaffDao employeeDao;

    @Resource
    private AuthRoleStaffDao roleEmployeeDao;

    /**
     * 保存员工及其角色关联
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveEmployee(StaffEntity employeeEntity, List<Long> roleIdList) {
        // 保存员工基本信息
        employeeDao.insert(employeeEntity);

        // 保存角色关联
        saveRoleEmployee(employeeEntity.getEmployeeId(), roleIdList);
    }

    /**
     * 更新员工及其角色关联
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateEmployee(StaffEntity employeeEntity, List<Long> roleIdList) {
        // 更新员工基本信息
        employeeDao.updateById(employeeEntity);

        // 删除旧的角色关联
        LambdaQueryWrapper<AuthRoleStaffEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuthRoleStaffEntity::getEmployeeId, employeeEntity.getEmployeeId());
        roleEmployeeDao.delete(wrapper);

        // 保存新的角色关联
        saveRoleEmployee(employeeEntity.getEmployeeId(), roleIdList);
    }

    /**
     * 保存员工角色关联
     */
    private void saveRoleEmployee(Long employeeId, List<Long> roleIdList) {
        if (roleIdList == null || roleIdList.isEmpty()) {
            return;
        }

        List<AuthRoleStaffEntity> roleEmployeeList = new ArrayList<>();
        for (Long roleId : roleIdList) {
            AuthRoleStaffEntity roleEmployee = new AuthRoleStaffEntity();
            roleEmployee.setEmployeeId(employeeId);
            roleEmployee.setRoleId(roleId);
            roleEmployeeList.add(roleEmployee);
        }

        for (AuthRoleStaffEntity roleEmployee : roleEmployeeList) {
            roleEmployeeDao.insert(roleEmployee);
        }
    }

    /**
     * 批量更新删除标记
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateDeleteFlag(List<Long> employeeIdList, Boolean deletedFlag) {
        if (employeeIdList == null || employeeIdList.isEmpty()) {
            return;
        }

        for (Long employeeId : employeeIdList) {
            StaffEntity employee = employeeDao.selectById(employeeId);
            if (employee != null) {
                employee.setDeletedFlag(deletedFlag);
                employeeDao.updateById(employee);
            }
        }
    }

    /**
     * 批量更新部门
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateDepartment(List<Long> employeeIdList, Long departmentId) {
        if (employeeIdList == null || employeeIdList.isEmpty()) {
            return;
        }

        for (Long employeeId : employeeIdList) {
            StaffEntity employee = employeeDao.selectById(employeeId);
            if (employee != null) {
                employee.setDepartmentId(departmentId);
                employeeDao.updateById(employee);
            }
        }
    }

    /**
     * 更新禁用状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateDisabledFlag(Long employeeId, Boolean disabledFlag) {
        StaffEntity employee = employeeDao.selectById(employeeId);
        if (employee != null) {
            employee.setDisabledFlag(disabledFlag);
            employeeDao.updateById(employee);
        }
    }
}
