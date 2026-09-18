package com.budaos.system.manager;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.budaos.system.dao.AuthRoleMenuDao;
import com.budaos.system.domain.entity.AuthRoleMenuEntity;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 角色菜单管理器
 *
 * @author budaos
 */
@Service
public class AuthRoleMenuManager extends ServiceImpl<AuthRoleMenuDao, AuthRoleMenuEntity> {

    @Resource
    private AuthRoleMenuDao roleMenuDao;

    /**
     * 更新角色菜单权限
     *
     * @param roleId 角色ID
     * @param roleMenuEntityList 角色菜单实体列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateRoleMenu(Long roleId, List<AuthRoleMenuEntity> roleMenuEntityList) {
        // 根据角色ID删除菜单权限
        roleMenuDao.deleteByRoleId(roleId);
        // 批量添加菜单权限
        saveBatch(roleMenuEntityList);
    }

}
