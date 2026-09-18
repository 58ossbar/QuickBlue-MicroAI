package com.budaos.api.business.feign;

import com.budaos.api.business.dto.AnnouncementDTO;
import com.budaos.common.core.domain.PageQuery;
import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.ApiResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通知公告服务 Feign 接口
 *
 * @author budaos
 */
@FeignClient(
    contextId = "noticeFeignClient",
    name = "QuickBlue-business",
    path = "/notice"
)
public interface AnnouncementFeignClient {

    /**
     * 根据ID获取公告信息
     *
     * @param noticeId 公告ID
     * @return 公告信息
     */
    @GetMapping("/{noticeId}")
    ApiResult<AnnouncementDTO> getById(@PathVariable("noticeId") Long noticeId);

    /**
     * 分页查询公告列表
     *
     * @param form 查询表单
     * @return 分页结果
     */
    @PostMapping("/query")
    ApiResult<PageResponse<AnnouncementDTO>> queryPage(@RequestBody PageQuery form);

    /**
     * 获取用户可见的公告列表
     *
     * @return 公告列表
     */
    @GetMapping("/visible")
    ApiResult<List<AnnouncementDTO>> getVisibleNoticeList();
}
