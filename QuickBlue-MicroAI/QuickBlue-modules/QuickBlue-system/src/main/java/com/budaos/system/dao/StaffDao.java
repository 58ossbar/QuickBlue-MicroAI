package com.budaos.system.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.budaos.system.annotation.DataPermission;
import com.budaos.common.core.enums.DataPermissionInTypeEnum;
import com.budaos.system.domain.entity.StaffEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 员工 DAO
 *
 * @author budaos
 */
@Mapper
public interface StaffDao extends BaseMapper<StaffEntity> {

    /**
     * 根据部门ID统计员工数量
     *
     * @param departmentId 部门ID
     * @param deletedFlag   删除标记
     * @return 员工数量
     */
    Integer countByDepartmentId(@Param("departmentId") Long departmentId, @Param("deletedFlag") Boolean deletedFlag);

    /**
     * 根据部门ID获取员工ID列表
     *
     * @param departmentId 部门ID
     * @param disabledFlag 是否禁用
     * @return 员工ID列表
     */
    List<Long> getEmployeeIdByDepartmentId(@Param("departmentId") Long departmentId, @Param("disabledFlag") Boolean disabledFlag);

    /**
     * 根据部门ID列表获取员工ID列表
     *
     * @param departmentIds 部门ID列表
     * @param disabledFlag 是否禁用
     * @return 员工ID列表
     */
    List<Long> getEmployeeIdByDepartmentIdList(@Param("departmentIds") List<Long> departmentIds, @Param("disabledFlag") Boolean disabledFlag);

    /**
     * 分页查询员工列表 - 应用数据权限
     * 使用 EMPLOYEE 类型的数据权限配置
     * configCode 对应 t_data_scope_config 表的 config_code 字段
     * 
     * 数据权限说明：
     * - ME（仅本人）：只显示本人
     * - DEPARTMENT（本部门）：显示本部门所有员工
     * - DEPARTMENT_AND_SUB（本部门及以下）：显示本部门及子部门所有员工
     * - ALL（全部）：显示所有员工
     *
     * @param page         分页参数
     * @param departmentIds 部门ID列表（可为空，为空时不限部门）
     * @return 员工列表
     */
    @DataPermission(
        configCode = "EMPLOYEE",
        whereInType = DataPermissionInTypeEnum.EMPLOYEE,
        joinSql = "employee_id IN (#employeeIds)",
        whereIndex = 0
    )
    IPage<StaffEntity> queryByDataScopePage(Page<StaffEntity> page, @Param("departmentIds") List<Long> departmentIds);

}

