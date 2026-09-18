package com.budaos.system.service;

import com.budaos.system.constant.DataPermissionViewTypeEnum;
import com.budaos.system.dao.StaffDao;
import com.budaos.system.dao.AuthRoleDataPermissionDao;
import com.budaos.system.dao.AuthRoleStaffDao;
import com.budaos.system.domain.entity.StaffEntity;
import com.budaos.system.domain.entity.AuthRoleDataPermissionEntity;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据范围视图服务
 *
 * 负责计算员工的数据权限可见范围
 *
 * @author budaos
 */
@Service
public class DataPermissionViewService {

    @Resource
    private AuthRoleStaffDao roleEmployeeDao;

    @Resource
    private AuthRoleDataPermissionDao roleDataScopeDao;

    @Resource
    private StaffDao employeeDao;

    @Resource
    private OrganizationService departmentService;

    /**
     * 获取某人可以查看的所有人员数据
     *
     * @param viewType 数据可见范围类型
     * @param employeeId 员工ID
     * @return 员工ID列表（null表示可以查看所有数据，空列表表示无数据权限）
     */
    public List<Long> getCanViewEmployeeId(DataPermissionViewTypeEnum viewType, Long employeeId) {
        if (DataPermissionViewTypeEnum.ME == viewType) {
            return this.getMeEmployeeIdList(employeeId);
        }
        if (DataPermissionViewTypeEnum.DEPARTMENT == viewType) {
            return this.getDepartmentEmployeeIdList(employeeId);
        }
        if (DataPermissionViewTypeEnum.DEPARTMENT_AND_SUB == viewType) {
            return this.getDepartmentAndSubEmployeeIdList(employeeId);
        }
        // 可以查看所有员工数据，返回null表示不限制
        return null;
    }

    /**
     * 获取某人可以查看的所有部门数据
     *
     * @param viewType 数据可见范围类型
     * @param employeeId 员工ID
     * @return 部门ID列表（null表示可以查看所有部门数据，空列表表示无数据权限）
     */
    public List<Long> getCanViewDepartmentId(DataPermissionViewTypeEnum viewType, Long employeeId) {
        if (DataPermissionViewTypeEnum.ME == viewType) {
            // 数据可见范围类型为本人时，只能查看自己所属的部门
            return this.getMeDepartmentIdList(employeeId);
        }
        if (DataPermissionViewTypeEnum.DEPARTMENT == viewType) {
            return this.getMeDepartmentIdList(employeeId);
        }
        if (DataPermissionViewTypeEnum.DEPARTMENT_AND_SUB == viewType) {
            return this.getDepartmentAndSubIdList(employeeId);
        }
        // 可以查看所有部门数据，返回null表示不限制
        return null;
    }

    /**
     * 获取员工所属部门ID列表
     *
     * @param employeeId 员工ID
     * @return 部门ID列表
     */
    public List<Long> getMeDepartmentIdList(Long employeeId) {
        StaffEntity employeeEntity = employeeDao.selectById(employeeId);
        if (employeeEntity == null || employeeEntity.getDepartmentId() == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(java.util.List.of(employeeEntity.getDepartmentId()));
    }

    /**
     * 获取员工所属部门及子部门ID列表
     *
     * @param employeeId 员工ID
     * @return 部门ID列表
     */
    public List<Long> getDepartmentAndSubIdList(Long employeeId) {
        StaffEntity employeeEntity = employeeDao.selectById(employeeId);
        if (employeeEntity == null || employeeEntity.getDepartmentId() == null) {
            return new ArrayList<>();
        }
        return departmentService.selfAndChildrenIdList(employeeEntity.getDepartmentId());
    }

    /**
     * 根据员工ID获取各数据范围最大的可见范围
     *
     * @param dataScopeTypeId 数据范围类型ID（使用 t_data_scope_config 表的 config_id）
     * @param employeeId 员工ID
     * @return 数据可见范围类型
     */
    public DataPermissionViewTypeEnum getEmployeeDataScopeViewType(Integer dataScopeTypeId, Long employeeId) {
        StaffEntity employeeEntity = employeeDao.selectById(employeeId);
        if (employeeEntity == null || employeeEntity.getEmployeeId() == null) {
            return DataPermissionViewTypeEnum.ME;
        }

        // 如果是超级管理员，则可查看全部
        if (employeeEntity.getAdministratorFlag()) {
            return DataPermissionViewTypeEnum.ALL;
        }

        List<Long> roleIdList = roleEmployeeDao.selectRoleIdByEmployeeId(employeeId);
        // 未设置角色，默认本人
        if (CollectionUtils.isEmpty(roleIdList)) {
            return DataPermissionViewTypeEnum.ME;
        }

        // 未设置角色数据范围，默认本人
        List<AuthRoleDataPermissionEntity> dataScopeRoleList = roleDataScopeDao.listByRoleIdList(roleIdList);
        if (CollectionUtils.isEmpty(dataScopeRoleList)) {
            return DataPermissionViewTypeEnum.ME;
        }

        // 根据config_id分组
        Map<Integer, List<AuthRoleDataPermissionEntity>> listMap = dataScopeRoleList.stream()
                .collect(Collectors.groupingBy(AuthRoleDataPermissionEntity::getDataScopeType));

        // 获取对应config_id的角色数据权限配置
        List<AuthRoleDataPermissionEntity> viewLevelList = listMap.getOrDefault(dataScopeTypeId, new ArrayList<>());
        if (CollectionUtils.isEmpty(viewLevelList)) {
            return DataPermissionViewTypeEnum.ME;
        }

        // 获取最大权限级别
        AuthRoleDataPermissionEntity maxLevel = viewLevelList.stream()
                .max(Comparator.comparing(e -> DataPermissionViewTypeEnum.getEnumByValue(e.getViewType()).getLevel()))
                .get();

        return DataPermissionViewTypeEnum.getEnumByValue(maxLevel.getViewType());
    }

    /**
     * 获取本人相关可查看员工ID
     *
     * @param employeeId 员工ID
     * @return 员工ID列表
     */
    private List<Long> getMeEmployeeIdList(Long employeeId) {
        return new ArrayList<>(java.util.List.of(employeeId));
    }

    /**
     * 获取本部门相关可查看员工ID
     *
     * @param employeeId 员工ID
     * @return 员工ID列表
     */
    private List<Long> getDepartmentEmployeeIdList(Long employeeId) {
        StaffEntity employeeEntity = employeeDao.selectById(employeeId);
        if (employeeEntity == null || employeeEntity.getDepartmentId() == null) {
            return new ArrayList<>();
        }
        return employeeDao.getEmployeeIdByDepartmentId(employeeEntity.getDepartmentId(), false);
    }

    /**
     * 获取本部门及下属子部门相关可查看员工ID
     *
     * @param employeeId 员工ID
     * @return 员工ID列表
     */
    private List<Long> getDepartmentAndSubEmployeeIdList(Long employeeId) {
        List<Long> allDepartmentIds = getDepartmentAndSubIdList(employeeId);
        if (CollectionUtils.isEmpty(allDepartmentIds)) {
            return new ArrayList<>();
        }
        return employeeDao.getEmployeeIdByDepartmentIdList(allDepartmentIds, false);
    }

}
