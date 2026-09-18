package com.budaos.api.system.feign;

import com.budaos.api.system.dto.StaffDTO;
import com.budaos.common.core.domain.PageQuery;
import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.ApiResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 员工服务 Feign 接口
 *
 * @author budaos
 */
@FeignClient(
    contextId = "employeeFeignClient",
    name = "QuickBlue-system",
    path = "/employee"
)
public interface StaffFeignClient {

    /**
     * 根据ID获取员工信息
     *
     * @param employeeId 员工ID
     * @return 员工信息
     */
    @GetMapping("/{employeeId}")
    ApiResult<StaffDTO> getById(@PathVariable("employeeId") Long employeeId);

    /**
     * 分页查询员工列表
     *
     * @param form 查询表单
     * @return 分页结果
     */
    @PostMapping("/query")
    ApiResult<PageResponse<StaffDTO>> queryPage(@RequestBody PageQuery form);

    /**
     * 根据员工UID获取员工信息
     *
     * @param employeeUid 员工UID
     * @return 员工信息
     */
    @GetMapping("/uid/{employeeUid}")
    ApiResult<StaffDTO> getByUid(@PathVariable("employeeUid") String employeeUid);
}
