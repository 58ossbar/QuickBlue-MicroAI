package com.budaos.api.business.feign;

import com.budaos.api.business.dto.RegionDTO;
import com.budaos.common.core.domain.ApiResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 区域服务 Feign 接口
 *
 * @author budaos
 */
@FeignClient(
    contextId = "regionFeignClient",
    name = "QuickBlue-business",
    path = "/region"
)
public interface RegionFeignClient {

    /**
     * 根据ID获取区域信息
     *
     * @param regionId 区域ID
     * @return 区域信息
     */
    @GetMapping("/{regionId}")
    ApiResult<RegionDTO> getById(@PathVariable("regionId") Long regionId);

    /**
     * 获取区域树形列表
     *
     * @return 区域树
     */
    @GetMapping("/tree")
    ApiResult<List<RegionDTO>> getTree();

    /**
     * 根据区域编码获取区域信息
     *
     * @param regionCode 区域编码
     * @return 区域信息
     */
    @GetMapping("/code/{regionCode}")
    ApiResult<RegionDTO> getByCode(@PathVariable("regionCode") String regionCode);
}
