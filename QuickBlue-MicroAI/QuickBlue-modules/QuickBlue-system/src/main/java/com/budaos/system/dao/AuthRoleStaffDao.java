package com.budaos.system.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.budaos.system.domain.entity.StaffEntity;
import com.budaos.system.domain.entity.AuthRoleStaffEntity;
import com.budaos.system.domain.form.AuthRoleStaffQueryForm;
import com.budaos.system.domain.vo.AuthRoleStaffVO;
import com.budaos.system.domain.vo.AuthRoleVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色员工 DAO
 *
 * @author budaos
 */
@Mapper
public interface AuthRoleStaffDao extends BaseMapper<AuthRoleStaffEntity> {

    /**
     * 根据员工ID查询所有角色
     *
     * @param employeeId 员工ID
     * @return 角色列表
     */
    List<AuthRoleVO> selectRoleByEmployeeId(@Param("employeeId") Long employeeId);

    /**
     * 根据员工ID查询所有角色ID
     *
     * @param employeeId 员工ID
     * @return 角色ID列表
     */
    List<Long> selectRoleIdByEmployeeId(@Param("employeeId") Long employeeId);

    /**
     * 根据员工ID列表查询所有角色关联
     *
     * @param employeeIdList 员工ID列表
     * @return 角色员工关联列表
     */
    List<AuthRoleStaffEntity> selectRoleIdByEmployeeIdList(@Param("employeeIdList") List<Long> employeeIdList);

    /**
     * 根据员工ID列表查询角色员工VO
     *
     * @param employeeIdList 员工ID列表
     * @return 角色员工VO列表
     */
    List<AuthRoleStaffVO> selectRoleByEmployeeIdList(@Param("employeeIdList") List<Long> employeeIdList);

    /**
     * 根据角色ID列表查询员工ID
     *
     * @param roleIdList 角色ID列表
     * @return 员工ID列表
     */
    List<Long> selectEmployeeIdByRoleIdList(@Param("roleIdList") List<Long> roleIdList);

    /**
     * 分页查询角色下的员工
     *
     * @param page      分页对象
     * @param queryForm 查询表单
     * @return 员工实体列表
     */
    List<StaffEntity> selectRoleEmployeeByName(Page page, @Param("queryForm") AuthRoleStaffQueryForm queryForm);

    /**
     * 根据角色ID查询所有员工
     *
     * @param roleId 角色ID
     * @return 员工实体列表
     */
    List<StaffEntity> selectEmployeeByRoleId(@Param("roleId") Long roleId);

    /**
     * 判断某个角色下是否存在员工
     *
     * @param roleId 角色ID
     * @return 存在数量
     */
    Integer existsByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据员工ID删除关联
     *
     * @param employeeId 员工ID
     */
    void deleteByEmployeeId(@Param("employeeId") Long employeeId);

    /**
     * 删除某个角色的所有关联
     *
     * @param roleId 角色ID
     */
    void deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据员工和角色删除关联
     *
     * @param employeeId 员工ID
     * @param roleId 角色ID
     */
    void deleteByEmployeeIdRoleId(@Param("employeeId") Long employeeId, @Param("roleId") Long roleId);

    /**
     * 批量删除某个角色下的某批员工的关联关系
     *
     * @param roleId      角色ID
     * @param employeeIds 员工ID集合
     */
    void batchDeleteEmployeeRole(@Param("roleId") Long roleId, @Param("employeeIds") List<Long> employeeIds);
}
