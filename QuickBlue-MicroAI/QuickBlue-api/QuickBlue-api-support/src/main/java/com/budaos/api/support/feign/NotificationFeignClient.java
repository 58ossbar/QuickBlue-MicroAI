package com.budaos.api.support.feign;

import com.budaos.api.support.dto.NotificationDTO;
import com.budaos.common.core.domain.PageQuery;
import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.ApiResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 消息服务 Feign 接口
 *
 * @author budaos
 */
@FeignClient(
    contextId = "messageFeignClient",
    name = "QuickBlue-support",
    path = "/message"
)
public interface NotificationFeignClient {

    /**
     * 分页查询消息列表
     *
     * @param form 查询表单
     * @return 分页结果
     */
    @PostMapping("/query")
    ApiResult<PageResponse<NotificationDTO>> queryPage(@RequestBody PageQuery form);

    /**
     * 获取未读消息数量
     *
     * @param employeeId 员工ID
     * @return 未读数量
     */
    @GetMapping("/unread/count/{employeeId}")
    ApiResult<Long> getUnreadCount(@PathVariable("employeeId") Long employeeId);

    /**
     * 标记消息已读
     *
     * @param messageId 消息ID
     * @return 标记结果
     */
    @PostMapping("/read/{messageId}")
    ApiResult<Void> markAsRead(@PathVariable("messageId") Long messageId);
}
