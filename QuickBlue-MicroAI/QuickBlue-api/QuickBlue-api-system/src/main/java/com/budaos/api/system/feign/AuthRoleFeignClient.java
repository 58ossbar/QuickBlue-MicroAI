package com.budaos.api.system.feign;

import com.budaos.api.system.dto.AuthRoleDTO;
import com.budaos.common.core.domain.ApiResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色服务 Feign 接口
 *
 * @author budaos
 */
@FeignClient(
    contextId = "roleFeignClient",
    name = "QuickBlue-system",
    path = "/role"
)
public interface AuthRoleFeignClient {

    /**
     * 根据ID获取角色信息
     *
     * @param roleId 角色ID
     * @return 角色信息
     */
    @GetMapping("/get/{roleId}")
    ApiResult<AuthRoleDTO> getById(@PathVariable("roleId") Long roleId);

    /**
     * 根据员工ID获取角色列表
     *
     * @param employeeId 员工ID
     * @return 角色列表
     */
    @GetMapping("/employee/{employeeId}")
    ApiResult<List<AuthRoleDTO>> listByEmployeeId(@PathVariable("employeeId") Long employeeId);
}
