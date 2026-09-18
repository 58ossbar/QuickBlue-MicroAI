package com.budaos.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.common.core.util.BeanCopyUtil;
import com.budaos.common.core.util.PageConvertUtil;
import com.budaos.system.dao.OrganizationDao;
import com.budaos.system.dao.StaffDao;
import com.budaos.system.dao.AuthRoleDao;
import com.budaos.system.dao.AuthRoleStaffDao;
import com.budaos.system.domain.entity.OrganizationEntity;
import com.budaos.system.domain.entity.StaffEntity;
import com.budaos.system.domain.entity.AuthRoleStaffEntity;
import com.budaos.system.domain.entity.AuthRoleEntity;
import com.budaos.system.domain.form.AuthRoleStaffQueryForm;
import com.budaos.system.domain.form.AuthRoleStaffUpdateForm;
import com.budaos.system.domain.vo.StaffVO;
import com.budaos.system.domain.vo.AuthRoleSelectedVO;
import com.budaos.system.domain.vo.AuthRoleVO;
import com.budaos.system.manager.AuthRoleStaffManager;
import com.budaos.system.service.AuthRoleStaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色员工关联服务实现
 *
 * @author budaos
 */
@Service
@RequiredArgsConstructor
public class AuthRoleStaffServiceImpl implements AuthRoleStaffService {

    private final AuthRoleStaffDao roleEmployeeDao;
    private final AuthRoleDao roleDao;
    private final StaffDao employeeDao;
    private final OrganizationDao departmentDao;
    private final AuthRoleStaffManager roleEmployeeManager;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchInsert(List<AuthRoleStaffEntity> roleEmployeeList) {
        roleEmployeeManager.saveBatch(roleEmployeeList);
    }

    @Override
    public ApiResult<PageResponse<StaffVO>> queryEmployee(AuthRoleStaffQueryForm queryForm) {
        Page page = PageConvertUtil.convert2PageQuery(queryForm);
        List<StaffEntity> employeeEntityList = roleEmployeeDao.selectRoleEmployeeByName(page, queryForm)
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 转换为VO
        List<StaffVO> employeeList = BeanCopyUtil.copyList(employeeEntityList, StaffVO.class);

        // 查询部门信息
        List<Long> departmentIdList = employeeList.stream()
                .filter(e -> e != null && e.getDepartmentId() != null)
                .map(StaffVO::getDepartmentId)
                .collect(Collectors.toList());

        if (CollUtil.isNotEmpty(departmentIdList)) {
            List<OrganizationEntity> departmentEntities = departmentDao.selectBatchIds(departmentIdList);
            Map<Long, String> departmentIdNameMap = departmentEntities.stream()
                    .collect(Collectors.toMap(OrganizationEntity::getDepartmentId, OrganizationEntity::getDepartmentName));
            employeeList.forEach(e -> {
                e.setDepartmentName(departmentIdNameMap.getOrDefault(e.getDepartmentId(), ""));
            });
        }

        PageResponse<StaffVO> pageResult = PageConvertUtil.convert2PageResult(page, employeeList);
        return ApiResult.ok(pageResult);
    }

    @Override
    public List<StaffVO> getAllEmployeeByRoleId(Long roleId) {
        List<StaffEntity> employeeList = roleEmployeeDao.selectEmployeeByRoleId(roleId);
        if (CollUtil.isEmpty(employeeList)) {
            return new ArrayList<>();
        }
        return employeeList.stream()
                .map(e -> {
                    StaffVO vo = new StaffVO();
                    BeanUtils.copyProperties(e, vo);
                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<String> removeRoleEmployee(Long employeeId, Long roleId) {
        if (employeeId == null || roleId == null) {
            return ApiResult.userErrorParam("员工ID和角色ID不能为空");
        }
        roleEmployeeDao.deleteByEmployeeIdRoleId(employeeId, roleId);
        return ApiResult.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<String> batchRemoveRoleEmployee(AuthRoleStaffUpdateForm updateForm) {
        roleEmployeeDao.batchDeleteEmployeeRole(updateForm.getRoleId(), new ArrayList<>(updateForm.getEmployeeIdList()));
        return ApiResult.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<String> batchAddRoleEmployee(AuthRoleStaffUpdateForm updateForm) {
        Long roleId = updateForm.getRoleId();

        // 已选择的员工ID列表
        Set<Long> selectedEmployeeIdList = updateForm.getEmployeeIdList();
        // 数据库里已有的员工ID列表
        List<Long> dbEmployeeIdList = roleEmployeeDao.selectEmployeeIdByRoleIdList(Collections.singletonList(roleId));

        // 从已选择的员工ID列表里过滤数据库里不存在的，即需要添加的员工ID
        Set<Long> addEmployeeIdList = selectedEmployeeIdList.stream()
                .filter(id -> !dbEmployeeIdList.contains(id))
                .collect(Collectors.toSet());

        // 添加角色员工
        if (CollUtil.isNotEmpty(addEmployeeIdList)) {
            List<AuthRoleStaffEntity> roleEmployeeList = addEmployeeIdList.stream()
                    .map(employeeId -> {
                        AuthRoleStaffEntity entity = new AuthRoleStaffEntity();
                        entity.setRoleId(roleId);
                        entity.setEmployeeId(employeeId);
                        return entity;
                    })
                    .collect(Collectors.toList());
            roleEmployeeManager.saveBatch(roleEmployeeList);
        }
        return ApiResult.ok();
    }

    @Override
    public List<AuthRoleSelectedVO> getRoleInfoListByEmployeeId(Long employeeId) {
        List<Long> roleIds = roleEmployeeDao.selectRoleIdByEmployeeId(employeeId);
        List<AuthRoleEntity> roleList = roleDao.selectList(null);
        List<AuthRoleSelectedVO> result = roleList.stream()
                .map(role -> {
                    AuthRoleSelectedVO vo = new AuthRoleSelectedVO();
                    BeanUtils.copyProperties(role, vo);
                    vo.setSelected(roleIds.contains(role.getRoleId()));
                    return vo;
                })
                .collect(Collectors.toList());
        return result;
    }

    @Override
    public List<AuthRoleVO> getRoleIdList(Long employeeId) {
        return roleEmployeeDao.selectRoleByEmployeeId(employeeId);
    }

    @Override
    public List<Long> selectRoleIdByEmployeeId(Long employeeId) {
        return roleEmployeeDao.selectRoleIdByEmployeeId(employeeId);
    }

    @Override
    public List<Long> selectEmployeeIdByRoleId(Long roleId) {
        return roleEmployeeDao.selectEmployeeIdByRoleIdList(Collections.singletonList(roleId));
    }

    @Override
    public Integer existsByRoleId(Long roleId) {
        return roleEmployeeDao.existsByRoleId(roleId);
    }
}
