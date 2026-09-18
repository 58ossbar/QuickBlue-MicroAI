package com.budaos.system.service;

import com.budaos.common.core.domain.ApiResult;
import com.budaos.system.domain.form.AuthRoleDataPermissionUpdateForm;
import com.budaos.system.domain.vo.AuthRoleDataPermissionVO;

import java.util.List;

/**
 * 角色数据权限服务接口
 *
 * @author budaos
 */
public interface AuthRoleDataPermissionService {

    /**
     * 获取某个角色的数据范围设置信息
     *
     * @param roleId 角色ID
     * @return 角色数据权限列表
     */
    ApiResult<List<AuthRoleDataPermissionVO>> getRoleDataScopeList(Long roleId);

    /**
     * 批量设置某个角色的数据范围设置信息
     *
     * @param roleDataScopeUpdateForm 角色数据权限更新表单
     * @return 操作结果
     */
    ApiResult<String> updateRoleDataScopeList(AuthRoleDataPermissionUpdateForm roleDataScopeUpdateForm);
}
