package com.budaos.system.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.budaos.system.annotation.DataPermission;
import com.budaos.common.core.enums.DataPermissionInTypeEnum;
import com.budaos.system.domain.entity.OrganizationEntity;
import com.budaos.system.domain.vo.OrganizationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 部门 DAO
 *
 * @author budaos
 */
@Mapper
public interface OrganizationDao extends BaseMapper<OrganizationEntity> {

    /**
     * 根据部门ID，查询此部门直接子部门的数量
     *
     * @param departmentId 部门ID
     * @return 子部门数量
     */
    Integer countSubDepartment(@Param("departmentId") Long departmentId);

    /**
     * 获取全部部门列表
     *
     * @return 部门列表
     */
    List<OrganizationVO> listAll();

    /**
     * 获取全部部门列表 - 应用数据权限
     * 使用 DEPARTMENT 类型的数据权限配置
     * configCode 对应 t_data_scope_config 表的 config_code 字段
     * 
     * 数据权限说明：
     * - ME（仅本人）：显示用户所属部门
     * - DEPARTMENT（本部门）：显示用户所属部门
     * - DEPARTMENT_AND_SUB（本部门及以下）：显示用户所属部门及所有子部门
     * - ALL（全部）：显示所有部门
     *
     * @return 部门列表
     */
    @DataPermission(
        configCode = "DEPARTMENT",
        whereInType = DataPermissionInTypeEnum.DEPARTMENT,
        joinSql = "t_department.department_id IN (#departmentIds)",
        whereIndex = 0
    )
    List<OrganizationVO> listAllWithDataScope();

    /**
     * 根据部门ID查询部门VO
     *
     * @param departmentId 部门ID
     * @return 部门VO
     */
    OrganizationVO selectDepartmentVO(@Param("departmentId") Long departmentId);
}
