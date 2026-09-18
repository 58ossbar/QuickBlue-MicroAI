package com.budaos.api.support.feign;

import com.budaos.api.support.dto.LoginRecordDTO;
import com.budaos.common.core.domain.PageQuery;
import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.ApiResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 登录日志服务 Feign 接口
 *
 * @author budaos
 */
@FeignClient(
    contextId = "loginLogFeignClient",
    name = "QuickBlue-support",
    path = "/loginlog"
)
public interface LoginRecordFeignClient {

    /**
     * 分页查询登录日志
     *
     * @param form 查询表单
     * @return 分页结果
     */
    @PostMapping("/query")
    ApiResult<PageResponse<LoginRecordDTO>> queryPage(@RequestBody PageQuery form);

    /**
     * 保存登录日志
     *
     * @param employeeId 员工ID
     * @param result    登录结果
     * @return 保存结果
     */
    @PostMapping("/save")
    ApiResult<Void> save(
            @RequestParam("employeeId") Long employeeId,
            @RequestParam("result") Integer result
    );
}
