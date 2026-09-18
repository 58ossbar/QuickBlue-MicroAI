package com.budaos.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.system.domain.entity.AuthRoleStaffEntity;
import com.budaos.system.domain.form.AuthRoleStaffQueryForm;
import com.budaos.system.domain.form.AuthRoleStaffUpdateForm;
import com.budaos.system.domain.vo.StaffVO;
import com.budaos.system.domain.vo.AuthRoleSelectedVO;
import com.budaos.system.domain.vo.AuthRoleVO;

import java.util.List;

/**
 * 角色员工关联服务接口
 *
 * @author budaos
 */
public interface AuthRoleStaffService {

    /**
     * 批量插入角色员工关联
     *
     * @param roleEmployeeList 角色员工关联列表
     */
    void batchInsert(List<AuthRoleStaffEntity> roleEmployeeList);

    /**
     * 查询某个角色下的员工列表（分页）
     *
     * @param queryForm 查询表单
     * @return 员工分页结果
     */
    ApiResult<PageResponse<StaffVO>> queryEmployee(AuthRoleStaffQueryForm queryForm);

    /**
     * 获取某个角色下的所有员工列表（无分页）
     *
     * @param roleId 角色ID
     * @return 员工列表
     */
    List<StaffVO> getAllEmployeeByRoleId(Long roleId);

    /**
     * 移除员工角色
     *
     * @param employeeId 员工ID
     * @param roleId     角色ID
     * @return 操作结果
     */
    ApiResult<String> removeRoleEmployee(Long employeeId, Long roleId);

    /**
     * 批量删除角色的成员员工
     *
     * @param updateForm 更新表单
     * @return 操作结果
     */
    ApiResult<String> batchRemoveRoleEmployee(AuthRoleStaffUpdateForm updateForm);

    /**
     * 批量添加角色的成员员工
     *
     * @param updateForm 更新表单
     * @return 操作结果
     */
    ApiResult<String> batchAddRoleEmployee(AuthRoleStaffUpdateForm updateForm);

    /**
     * 通过员工ID获取员工角色（含选中状态）
     *
     * @param employeeId 员工ID
     * @return 角色选择列表
     */
    List<AuthRoleSelectedVO> getRoleInfoListByEmployeeId(Long employeeId);

    /**
     * 根据员工ID查询角色ID集合
     *
     * @param employeeId 员工ID
     * @return 角色列表
     */
    List<AuthRoleVO> getRoleIdList(Long employeeId);

    /**
     * 根据员工ID查询所有角色ID
     *
     * @param employeeId 员工ID
     * @return 角色ID列表
     */
    List<Long> selectRoleIdByEmployeeId(Long employeeId);

    /**
     * 根据角色ID查询所有员工ID
     *
     * @param roleId 角色ID
     * @return 员工ID集合
     */
    List<Long> selectEmployeeIdByRoleId(Long roleId);

    /**
     * 判断某个角色下是否存在员工
     *
     * @param roleId 角色ID
     * @return 存在数量
     */
    Integer existsByRoleId(Long roleId);
}
