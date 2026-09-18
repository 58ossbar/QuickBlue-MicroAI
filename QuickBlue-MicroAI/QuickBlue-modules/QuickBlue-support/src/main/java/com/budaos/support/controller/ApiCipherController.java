package com.budaos.support.controller;

import com.budaos.common.core.domain.ApiResult;
import com.budaos.support.service.ApiCipherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API 加密/解密控制器
 *
 * @author budaos
 */
@Tag(name = "API加密服务", description = "API加密/解密接口")
@RestController
@RequestMapping("/apiEncrypt")
public class ApiCipherController {

    @Resource
    private ApiCipherService apiEncryptService;

    /**
     * 加密数据
     */
    @GetMapping("/encrypt")
    @Operation(summary = "加密数据")
    public ApiResult<String> encrypt(@RequestParam("data") String data) {
        String encrypted = apiEncryptService.encrypt(data);
        return ApiResult.ok(encrypted);
    }

    /**
     * 解密数据
     */
    @GetMapping("/decrypt")
    @Operation(summary = "解密数据")
    public ApiResult<String> decrypt(@RequestParam("data") String data) {
        String decrypted = apiEncryptService.decrypt(data);
        return ApiResult.ok(decrypted);
    }

    /**
     * 测试请求加密
     * 前端加密请求参数，后端解密后返回
     */
    @PostMapping("/testRequestEncrypt")
    @Operation(summary = "测试请求加密")
    public ApiResult<Map<String, Object>> testRequestEncrypt(@RequestBody Map<String, String> request) {
        // 前端 postEncryptRequest 发送格式: { encryptData: "加密后的字符串" }
        String encryptedData = request.get("encryptData");
        
        // 解密前端传来的加密数据
        String decrypted = apiEncryptService.decrypt(encryptedData);
        
        Map<String, Object> result = new HashMap<>();
        result.put("original", encryptedData);
        result.put("decrypted", decrypted);
        result.put("success", true);
        
        return ApiResult.ok(result);
    }

    /**
     * 测试响应加密
     * 返回加密后的响应数据
     */
    @PostMapping("/testResponseEncrypt")
    @Operation(summary = "测试响应加密")
    public ApiResult<String> testResponseEncrypt(@RequestBody Map<String, Object> request) {
        // 前端 postRequest 发送格式: { name: "xxx", age: 100 }
        String data = request.toString();
        String encrypted = apiEncryptService.encrypt(data);
        return ApiResult.ok(encrypted);
    }

    /**
     * 测试解密后加密
     * 先解密传入的数据，再重新加密返回
     */
    @PostMapping("/testDecryptAndEncrypt")
    @Operation(summary = "测试解密后加密")
    public ApiResult<String> testDecryptAndEncrypt(@RequestBody Map<String, String> request) {
        // 前端 postEncryptRequest 发送格式: { encryptData: "加密后的字符串" }
        String encryptedData = request.get("encryptData");
        
        // 先解密
        String decrypted = apiEncryptService.decrypt(encryptedData);
        // 再加密
        String reEncrypted = apiEncryptService.encrypt(decrypted);
        return ApiResult.ok(reEncrypted);
    }

    /**
     * 测试数组数据加密
     * 对数组中的每个元素进行加密
     */
    @PostMapping("/testArray")
    @Operation(summary = "测试数组数据加密")
    public ApiResult<List<String>> testArray(@RequestBody Map<String, String> request) {
        // 前端 postEncryptRequest 发送格式: { encryptData: "加密后的字符串" }
        String encryptedData = request.get("encryptData");
        
        // 解密得到 JSON 数组字符串
        String decrypted = apiEncryptService.decrypt(encryptedData);
        
        // 解析 JSON 数组并加密每个元素
        // 假设解密后是 JSON 数组格式: [{"name":"卓1","age":1}, ...]
        String encrypted = apiEncryptService.encrypt(decrypted);
        
        return ApiResult.ok(Arrays.asList(encrypted));
    }
}
