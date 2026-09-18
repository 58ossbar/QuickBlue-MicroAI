package com.budaos.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.system.domain.entity.OrganizationEntity;
import com.budaos.system.domain.form.OrganizationAddForm;
import com.budaos.system.domain.form.OrganizationUpdateForm;
import com.budaos.system.domain.vo.OrganizationTreeVO;
import com.budaos.system.domain.vo.OrganizationVO;

import java.util.List;

/**
 * 部门服务接口
 *
 * @author budaos
 */
public interface OrganizationService extends IService<OrganizationEntity> {

    /**
     * 查询部门树形列表（不带数据权限，使用缓存）
     *
     * @return 部门树形列表
     */
    ApiResult<List<OrganizationTreeVO>> departmentTree();

    /**
     * 查询部门树形列表（带数据权限控制）
     * 
     * 数据权限说明：
     * - ME（仅本人）：显示用户所属部门
     * - DEPARTMENT（本部门）：显示用户所属部门
     * - DEPARTMENT_AND_SUB（本部门及以下）：显示用户所属部门及所有子部门
     * - ALL（全部）：显示所有部门
     *
     * @return 部门树形列表
     */
    ApiResult<List<OrganizationTreeVO>> departmentTreeWithDataScope();

    /**
     * 添加部门
     *
     * @param addForm 添加表单
     * @return 操作结果
     */
    ApiResult<String> addDepartment(OrganizationAddForm addForm);

    /**
     * 更新部门
     *
     * @param updateForm 更新表单
     * @return 操作结果
     */
    ApiResult<String> updateDepartment(OrganizationUpdateForm updateForm);

    /**
     * 删除部门
     *
     * @param departmentId 部门ID
     * @return 操作结果
     */
    ApiResult<String> deleteDepartment(Long departmentId);

    /**
     * 查询所有部门
     *
     * @return 部门列表
     */
    List<OrganizationVO> listAll();

    /**
     * 查询所有部门（应用数据权限）
     *
     * @return 部门列表
     */
    List<OrganizationVO> listAllWithDataScope();

    /**
     * 根据ID查询部门
     *
     * @param departmentId 部门ID
     * @return 部门VO
     */
    OrganizationVO getDepartmentById(Long departmentId);

    /**
     * 获取部门路径
     *
     * @param departmentId 部门ID
     * @return 部门路径
     */
    String getDepartmentPath(Long departmentId);

    /**
     * 获取自身及所有下级部门的ID列表
     *
     * @param departmentId 部门ID
     * @return 部门ID列表
     */
    List<Long> selfAndChildrenIdList(Long departmentId);
}
