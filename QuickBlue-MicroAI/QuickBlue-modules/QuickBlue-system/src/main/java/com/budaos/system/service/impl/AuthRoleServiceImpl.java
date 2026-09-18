package com.budaos.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.common.core.util.BeanCopyUtil;
import com.budaos.system.dao.AuthRoleDao;
import com.budaos.system.dao.AuthRoleStaffDao;
import com.budaos.system.dao.AuthRoleMenuDao;
import com.budaos.system.domain.entity.AuthRoleEntity;
import com.budaos.system.domain.form.AuthRoleAddForm;
import com.budaos.system.domain.form.AuthRoleUpdateForm;
import com.budaos.system.domain.vo.AuthRoleVO;
import com.budaos.system.manager.RoleManager;
import com.budaos.system.service.AuthRoleService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 角色服务实现
 *
 * @author budaos
 */
@Slf4j
@Service
public class AuthRoleServiceImpl extends ServiceImpl<AuthRoleDao, AuthRoleEntity> implements AuthRoleService {

    @Resource
    private AuthRoleDao roleDao;

    @Resource
    private AuthRoleStaffDao roleEmployeeDao;

    @Resource
    private AuthRoleMenuDao roleMenuDao;

    @Resource
    private RoleManager roleManager;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<String> addRole(AuthRoleAddForm addForm) {
        // 校验角色名称是否重复
        AuthRoleEntity existRole = roleDao.getByRoleName(addForm.getRoleName());
        if (existRole != null) {
            return ApiResult.userErrorParam("角色名称已存在");
        }

        // 校验角色编码是否重复
        existRole = roleDao.getByRoleCode(addForm.getRoleCode());
        if (existRole != null) {
            return ApiResult.userErrorParam("角色编码已存在，重复的角色为：" + existRole.getRoleName());
        }

        // 创建角色实体
        AuthRoleEntity roleEntity = BeanCopyUtil.copyProperties(addForm, AuthRoleEntity.class);

        roleDao.insert(roleEntity);

        log.info("添加角色成功，角色ID: {}, 角色名称: {}", roleEntity.getRoleId(), roleEntity.getRoleName());

        return ApiResult.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<String> deleteRole(Long roleId) {
        AuthRoleEntity roleEntity = roleDao.selectById(roleId);
        if (roleEntity == null) {
            return ApiResult.userErrorParam("角色不存在");
        }

        // 检查角色下是否存在员工
        Integer existsCount = roleEmployeeDao.existsByRoleId(roleId);
        if (existsCount != null && existsCount > 0) {
            return ApiResult.userErrorParam("该角色下存在员工，无法删除");
        }

        // 删除角色
        roleDao.deleteById(roleId);

        // 删除角色菜单关联
        roleMenuDao.deleteByRoleId(roleId);

        // 删除角色员工关联
        roleEmployeeDao.deleteByRoleId(roleId);

        log.info("删除角色成功，角色ID: {}, 角色名称: {}", roleId, roleEntity.getRoleName());

        return ApiResult.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<String> updateRole(AuthRoleUpdateForm updateForm) {
        AuthRoleEntity roleEntity = roleDao.selectById(updateForm.getRoleId());
        if (roleEntity == null) {
            return ApiResult.userErrorParam("角色不存在");
        }

        // 检查角色名称唯一性
        AuthRoleEntity existRole = roleDao.getByRoleName(updateForm.getRoleName());
        if (existRole != null && !existRole.getRoleId().equals(updateForm.getRoleId())) {
            return ApiResult.userErrorParam("角色名称已存在");
        }

        // 检查角色编码唯一性
        if (StrUtil.isNotBlank(updateForm.getRoleCode())) {
            existRole = roleDao.getByRoleCode(updateForm.getRoleCode());
            if (existRole != null && !existRole.getRoleId().equals(updateForm.getRoleId())) {
                return ApiResult.userErrorParam("角色编码已存在，重复的角色为：" + existRole.getRoleName());
            }
        }

        // 更新角色
        AuthRoleEntity role = BeanCopyUtil.copyProperties(updateForm, AuthRoleEntity.class);
        roleDao.updateById(role);

        log.info("更新角色成功，角色ID: {}", updateForm.getRoleId());

        return ApiResult.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<String> updateRoleMenu(Long roleId, List<Long> menuIdList) {
        // 验证角色是否存在
        AuthRoleEntity roleEntity = roleDao.selectById(roleId);
        if (roleEntity == null) {
            return ApiResult.userErrorParam("角色不存在");
        }

        // 更新角色菜单关联
        roleManager.updateRoleMenu(roleId, menuIdList);

        log.info("更新角色菜单权限成功，角色ID: {}, 菜单数量: {}", roleId, menuIdList != null ? menuIdList.size() : 0);

        return ApiResult.ok();
    }

    @Override
    public ApiResult<AuthRoleVO> getRoleById(Long roleId) {
        AuthRoleEntity roleEntity = roleDao.selectById(roleId);
        if (roleEntity == null) {
            return ApiResult.userErrorParam("角色不存在");
        }

        AuthRoleVO roleVO = BeanCopyUtil.copyProperties(roleEntity, AuthRoleVO.class);
        return ApiResult.ok(roleVO);
    }

    @Override
    public ApiResult<List<AuthRoleVO>> getAllRole() {
        LambdaQueryWrapper<AuthRoleEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(AuthRoleEntity::getCreateTime);

        List<AuthRoleEntity> roleEntityList = roleDao.selectList(wrapper);
        List<AuthRoleVO> roleVOList = BeanCopyUtil.copyList(roleEntityList, AuthRoleVO.class);

        return ApiResult.ok(roleVOList);
    }
}
