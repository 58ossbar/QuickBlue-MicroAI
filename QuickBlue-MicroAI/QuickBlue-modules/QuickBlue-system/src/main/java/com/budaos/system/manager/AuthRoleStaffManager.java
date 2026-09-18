package com.budaos.system.manager;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.budaos.system.dao.AuthRoleStaffDao;
import com.budaos.system.domain.entity.AuthRoleStaffEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 角色员工关联管理器
 *
 * @author budaos
 */
@Component
@RequiredArgsConstructor
public class AuthRoleStaffManager extends ServiceImpl<AuthRoleStaffDao, AuthRoleStaffEntity> {

}
