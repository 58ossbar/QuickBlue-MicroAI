package com.budaos.api.support.feign;

import com.budaos.common.core.domain.ApiResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * API 加密服务 Feign Client
 *
 * @author budaos
 */
@FeignClient(contextId = "apiEncryptFeignClient", value = "QuickBlue-support")
public interface ApiCipherFeignClient {

    /**
     * 加密数据
     *
     * @param data 原始数据
     * @return 加密后的数据（Base64）
     */
    @GetMapping("/support/apiEncrypt/encrypt")
    ApiResult<String> encrypt(@RequestParam("data") String data);

    /**
     * 解密数据
     *
     * @param data 加密后的数据（Base64）
     * @return 原始数据
     */
    @GetMapping("/support/apiEncrypt/decrypt")
    ApiResult<String> decrypt(@RequestParam("data") String data);
}
