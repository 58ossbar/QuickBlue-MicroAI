package com.budaos.api.support.feign;

import com.budaos.api.support.dto.QRCodeApiResult;
import com.budaos.common.core.domain.ApiResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 二维码服务 Feign 接口
 *
 * @author budaos
 */
@FeignClient(
    contextId = "qrCodeFeignClient",
    name = "QuickBlue-support",
    path = "/qrcode"
)
public interface QRCodeFeignClient {

    /**
     * 生成二维码
     *
     * @param content      二维码内容
     * @param size         尺寸
     * @param foregroundColor 前景色
     * @param backgroundColor 背景色
     * @return 二维码响应
     */
    @PostMapping("/generate")
    ApiResult<QRCodeApiResult> generate(
            @RequestParam("content") String content,
            @RequestParam(value = "size", defaultValue = "300") Integer size,
            @RequestParam(value = "foregroundColor", required = false) String foregroundColor,
            @RequestParam(value = "backgroundColor", required = false) String backgroundColor
    );

    /**
     * 解析二维码
     *
     * @param base64Image Base64编码的二维码图片
     * @return 解析结果
     */
    @PostMapping("/decode")
    ApiResult<String> decode(@RequestParam("base64Image") String base64Image);
}
