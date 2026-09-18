package com.budaos.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.system.domain.entity.AuthRoleEntity;
import com.budaos.system.domain.form.AuthRoleAddForm;
import com.budaos.system.domain.form.AuthRoleUpdateForm;
import com.budaos.system.domain.vo.AuthRoleVO;

import java.util.List;

/**
 * 角色服务接口
 *
 * @author budaos
 */
public interface AuthRoleService extends IService<AuthRoleEntity> {

    /**
     * 添加角色
     *
     * @param addForm 添加表单
     * @return 操作结果
     */
    ApiResult<String> addRole(AuthRoleAddForm addForm);

    /**
     * 删除角色
     *
     * @param roleId 角色ID
     * @return 操作结果
     */
    ApiResult<String> deleteRole(Long roleId);

    /**
     * 更新角色
     *
     * @param updateForm 更新表单
     * @return 操作结果
     */
    ApiResult<String> updateRole(AuthRoleUpdateForm updateForm);

    /**
     * 更新角色菜单权限
     *
     * @param roleId 角色ID
     * @param menuIdList 菜单ID列表
     * @return 操作结果
     */
    ApiResult<String> updateRoleMenu(Long roleId, List<Long> menuIdList);

    /**
     * 根据ID查询角色
     *
     * @param roleId 角色ID
     * @return 角色信息
     */
    ApiResult<AuthRoleVO> getRoleById(Long roleId);

    /**
     * 查询所有角色
     *
     * @return 角色列表
     */
    ApiResult<List<AuthRoleVO>> getAllRole();
}
