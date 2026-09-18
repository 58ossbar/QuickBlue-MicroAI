package com.budaos.api.system.feign;

import com.budaos.api.system.dto.NavMenuDTO;
import com.budaos.common.core.domain.ApiResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单服务 Feign 接口
 *
 * @author budaos
 */
@FeignClient(
    contextId = "menuFeignClient",
    name = "QuickBlue-system",
    path = "/menu"
)
public interface NavMenuFeignClient {

    /**
     * 根据ID获取菜单信息
     *
     * @param menuId 菜单ID
     * @return 菜单信息
     */
    @GetMapping("/{menuId}")
    ApiResult<NavMenuDTO> getById(@PathVariable("menuId") Long menuId);

    /**
     * 获取当前用户的菜单树
     *
     * @return 菜单树
     */
    @GetMapping("/tree")
    ApiResult<List<NavMenuDTO>> getUserMenuTree();

    /**
     * 根据员工ID获取菜单权限ID列表
     *
     * @param employeeId 员工ID
     * @return 菜单ID列表
     */
    @GetMapping("/employee/{employeeId}/menuIds")
    ApiResult<List<Long>> getMenuIdsByEmployeeId(@PathVariable("employeeId") Long employeeId);
}
