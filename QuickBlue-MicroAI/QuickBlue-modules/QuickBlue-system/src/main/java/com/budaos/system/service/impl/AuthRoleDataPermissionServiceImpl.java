package com.budaos.system.service.impl;

import com.budaos.common.core.code.UserErrorCodes;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.common.core.util.BeanCopyUtil;
import com.budaos.system.domain.entity.AuthRoleDataPermissionEntity;
import com.budaos.system.domain.form.AuthRoleDataPermissionUpdateForm;
import com.budaos.system.domain.vo.AuthRoleDataPermissionVO;
import com.budaos.system.manager.AuthRoleDataPermissionManager;
import com.budaos.system.service.AuthRoleDataPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 角色数据权限服务实现
 *
 * @author budaos
 */
@Service
@RequiredArgsConstructor
public class AuthRoleDataPermissionServiceImpl implements AuthRoleDataPermissionService {

    private final AuthRoleDataPermissionManager roleDataScopeManager;

    @Override
    public ApiResult<List<AuthRoleDataPermissionVO>> getRoleDataScopeList(Long roleId) {
        List<AuthRoleDataPermissionEntity> roleDataScopeEntityList = roleDataScopeManager.getBaseMapper().listByRoleId(roleId);
        if (CollectionUtils.isEmpty(roleDataScopeEntityList)) {
            return ApiResult.ok(new ArrayList<>());
        }
        List<AuthRoleDataPermissionVO> roleDataScopeList = BeanCopyUtil.copyList(roleDataScopeEntityList, AuthRoleDataPermissionVO.class);
        return ApiResult.ok(roleDataScopeList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<String> updateRoleDataScopeList(AuthRoleDataPermissionUpdateForm roleDataScopeUpdateForm) {
        List<AuthRoleDataPermissionUpdateForm.RoleUpdateDataScopeListFormItem> batchSetList = roleDataScopeUpdateForm.getDataScopeItemList();
        if (CollectionUtils.isEmpty(batchSetList)) {
            return ApiResult.error(UserErrorCodes.PARAM_ERROR, "缺少配置信息");
        }
        List<AuthRoleDataPermissionEntity> roleDataScopeEntityList = BeanCopyUtil.copyList(batchSetList, AuthRoleDataPermissionEntity.class);
        roleDataScopeEntityList.forEach(e -> e.setRoleId(roleDataScopeUpdateForm.getRoleId()));
        roleDataScopeManager.getBaseMapper().deleteByRoleId(roleDataScopeUpdateForm.getRoleId());
        roleDataScopeManager.saveBatch(roleDataScopeEntityList);
        return ApiResult.ok();
    }
}
