package com.budaos.api.system.feign;

import com.budaos.api.system.dto.OrganizationDTO;
import com.budaos.common.core.domain.ApiResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门服务 Feign 接口
 *
 * @author budaos
 */
@FeignClient(
    contextId = "departmentFeignClient",
    name = "QuickBlue-system",
    path = "/department"
)
public interface OrganizationFeignClient {

    /**
     * 根据ID获取部门信息
     *
     * @param departmentId 部门ID
     * @return 部门信息
     */
    @GetMapping("/{departmentId}")
    ApiResult<OrganizationDTO> getById(@PathVariable("departmentId") Long departmentId);

    /**
     * 获取部门树形列表
     *
     * @return 部门树
     */
    @GetMapping("/treeList")
    ApiResult<List<OrganizationDTO>> getTree();

    /**
     * 获取自身及所有下级部门的ID列表（Feign调用专用）
     *
     * @param departmentId 部门ID
     * @return 部门ID列表
     */
    @GetMapping("/selfAndChildren/{departmentId}")
    ApiResult<List<Long>> getSelfAndChildrenIdList(@PathVariable("departmentId") Long departmentId);
}
