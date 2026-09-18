package com.budaos.system.manager;

import com.budaos.common.core.constant.CacheKeyConstants;
import com.budaos.common.core.domain.CurrentUser;
import com.budaos.common.core.domain.UserAuthority;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.common.core.util.BeanCopyUtil;
import com.budaos.system.domain.SessionEmployee;
import com.budaos.system.domain.entity.StaffEntity;
import com.budaos.system.domain.vo.NavMenuVO;
import com.budaos.system.domain.vo.AuthRoleVO;
import com.budaos.system.service.OrganizationService;
import com.budaos.system.service.StaffService;
import com.budaos.system.service.AuthRoleStaffService;
import com.budaos.system.service.AuthRoleMenuService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 登录缓存管理
 *
 * @author budaos
 */
@Slf4j
@Service
public class LoginCacheManager {

    @Resource
    private OrganizationService departmentService;

    @Resource
    private StaffService employeeService;

    @Resource
    private AuthRoleStaffService roleEmployeeService;

    @Resource
    private AuthRoleMenuService roleMenuService;

    @Resource
    private com.budaos.api.support.feign.FileStorageFeignClient fileStorageFeignClient;

    /**
     * 获取请求用户信息
     */
    @Cacheable(CacheKeyConstants.Login.REQUEST_EMPLOYEE)
    public SessionEmployee getRequestEmployee(Long requestEmployeeId) {
        if (requestEmployeeId == null) {
            return null;
        }
        // 员工基本信息
        StaffEntity employeeEntity = employeeService.getById(requestEmployeeId);
        if (employeeEntity == null) {
            return null;
        }

        return this.loadLoginInfo(employeeEntity);
    }

    /**
     * 获取登录的用户信息
     */
    @CachePut(value = CacheKeyConstants.Login.REQUEST_EMPLOYEE, key = "#employeeEntity.employeeId")
    public SessionEmployee loadLoginInfo(StaffEntity employeeEntity) {
        // 基础信息
        SessionEmployee requestEmployee = BeanCopyUtil.copy(employeeEntity, SessionEmployee.class);

        // 部门信息
        com.budaos.system.domain.vo.OrganizationVO department = departmentService.getDepartmentById(employeeEntity.getDepartmentId());
        requestEmployee.setDepartmentName(department == null ? "" : department.getDepartmentName());

        // 头像信息
        String avatar = employeeEntity.getAvatar();
        if (StringUtils.isNotBlank(avatar)) {
            ApiResult<String> getFileUrl = fileStorageFeignClient.getFileUrl(avatar);
            if (BooleanUtils.isTrue(getFileUrl.getOk())) {
                requestEmployee.setAvatar(getFileUrl.getData());
            }
        }
        return requestEmployee;
    }

    /**
     * 获取用户的权限（包含 角色列表、权限列表）
     */
    @Cacheable(CacheKeyConstants.Login.USER_PERMISSION)
    public UserAuthority getUserPermission(Long employeeId) {
        if (null == employeeId) {
            return null;
        }

        return this.loadUserPermission(employeeId);
    }

    /**
     * 获取用户的权限（包含 角色列表、权限列表）
     */
    @CachePut(CacheKeyConstants.Login.USER_PERMISSION)
    public UserAuthority loadUserPermission(Long employeeId) {
        UserAuthority userPermission = new UserAuthority();
        userPermission.setPermissionList(new ArrayList<>());
        userPermission.setRoleList(new ArrayList<>());

        // 角色列表
        List<AuthRoleVO> roleList = roleEmployeeService.getRoleIdList(employeeId);
        userPermission.getRoleList().addAll(roleList.stream().map(AuthRoleVO::getRoleCode).collect(Collectors.toSet()));

        // 前端菜单和功能点清单
        StaffEntity employeeEntity = employeeService.getById(employeeId);

        List<NavMenuVO> menuAndPointsList = roleMenuService.getMenuList(roleList.stream().map(AuthRoleVO::getRoleId).collect(Collectors.toList()), employeeEntity.getAdministratorFlag());

        // 权限列表
        HashSet<String> permissionSet = new HashSet<>();
        for (NavMenuVO menu : menuAndPointsList) {
            if (menu.getPermsType() == null) {
                continue;
            }

            String perms = menu.getApiPerms();
            if (StringUtils.isEmpty(perms)) {
                continue;
            }
            //接口权限
            String[] split = perms.split(",");
            permissionSet.addAll(Arrays.asList(split));
        }
        userPermission.getPermissionList().addAll(permissionSet);

        return userPermission;
    }

    /**
     * 清除用户权限
     */
    @CacheEvict(value = CacheKeyConstants.Login.USER_PERMISSION)
    public void clearUserPermission(Long employeeId) {

    }

    /**
     * 清除用户角色缓存
     */
    @CacheEvict(value = CacheKeyConstants.Login.USER_PERMISSION + "_roles")
    public void clearUserRoleCache(Long employeeId) {

    }

    /**
     * 获取用户的权限列表
     */
    @Cacheable(CacheKeyConstants.Login.USER_PERMISSION)
    public List<String> getUserPermissionList(Long employeeId) {
        UserAuthority userPermission = loadUserPermission(employeeId);
        return userPermission != null ? userPermission.getPermissionList() : List.of();
    }

    /**
     * 获取用户的角色列表
     */
    @Cacheable(value = CacheKeyConstants.Login.USER_PERMISSION + "_roles", key = "#employeeId")
    public List<String> getUserRoleList(Long employeeId) {
        UserAuthority userPermission = loadUserPermission(employeeId);
        return userPermission != null ? userPermission.getRoleList() : List.of();
    }

    /**
     * 清除用户登录信息
     */
    @CacheEvict(value = CacheKeyConstants.Login.REQUEST_EMPLOYEE)
    public void clearUserLoginInfo(Long employeeId) {

    }
}
