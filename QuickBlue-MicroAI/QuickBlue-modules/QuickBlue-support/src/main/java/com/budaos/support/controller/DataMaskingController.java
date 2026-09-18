package com.budaos.support.controller;

import com.budaos.common.core.domain.ApiResult;
import com.budaos.support.service.DataMaskingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 数据脱敏控制器
 *
 * @author budaos
 */
@Tag(name = "数据脱敏服务", description = "数据脱敏演示接口")
@RestController
@RequestMapping("/dataMasking")
public class DataMaskingController {

    @Resource
    private DataMaskingService dataMaskingService;

    /**
     * 脱敏演示查询
     * 返回各类脱敏后的数据示例
     */
    @GetMapping("/demo/query")
    @Operation(summary = "脱敏演示查询")
    public ApiResult<List<Map<String, Object>>> demoQuery() {
        List<Map<String, Object>> result = new ArrayList<>();

        // 演示数据：原始值与脱敏值对比
        Map<String, Object> row1 = new LinkedHashMap<>();
        row1.put("userId", 1);
        row1.put("other", "原始值");
        row1.put("phone", "13812345678");
        row1.put("idCard", "110101199001011234");
        row1.put("password", "abc123456");
        row1.put("email", "zhangsan@example.com");
        row1.put("carLicense", "京A12345");
        row1.put("bankCard", "6222021234567890123");
        row1.put("address", "北京市朝阳区望京街道科技园路100号");

        Map<String, Object> row2 = new LinkedHashMap<>();
        row2.put("userId", 2);
        row2.put("other", "脱敏后");
        row2.put("phone", dataMaskingService.maskPhone("13812345678"));
        row2.put("idCard", dataMaskingService.maskIdCard("110101199001011234"));
        row2.put("password", "******");
        row2.put("email", dataMaskingService.maskEmail("zhangsan@example.com"));
        row2.put("carLicense", "京A***5");
        row2.put("bankCard", dataMaskingService.maskBankCard("6222021234567890123"));
        row2.put("address", dataMaskingService.maskAddress("北京市朝阳区望京街道科技园路100号"));

        Map<String, Object> row3 = new LinkedHashMap<>();
        row3.put("userId", 3);
        row3.put("other", "原始值");
        row3.put("phone", "15987654321");
        row3.put("idCard", "310101198512125678");
        row3.put("password", "password123");
        row3.put("email", "lisi@company.com");
        row3.put("carLicense", "沪B67890");
        row3.put("bankCard", "6217001234567891234");
        row3.put("address", "上海市浦东新区陆家嘴金融中心88号");

        Map<String, Object> row4 = new LinkedHashMap<>();
        row4.put("userId", 4);
        row4.put("other", "脱敏后");
        row4.put("phone", dataMaskingService.maskPhone("15987654321"));
        row4.put("idCard", dataMaskingService.maskIdCard("310101198512125678"));
        row4.put("password", "******");
        row4.put("email", dataMaskingService.maskEmail("lisi@company.com"));
        row4.put("carLicense", "沪B***0");
        row4.put("bankCard", dataMaskingService.maskBankCard("6217001234567891234"));
        row4.put("address", dataMaskingService.maskAddress("上海市浦东新区陆家嘴金融中心88号"));

        result.add(row1);
        result.add(row2);
        result.add(row3);
        result.add(row4);

        return ApiResult.ok(result);
    }

    /**
     * 手机号脱敏
     */
    @GetMapping("/phone")
    @Operation(summary = "手机号脱敏")
    public ApiResult<String> maskPhone(@RequestParam("phone") String phone) {
        return ApiResult.ok(dataMaskingService.maskPhone(phone));
    }

    /**
     * 身份证号脱敏
     */
    @GetMapping("/idCard")
    @Operation(summary = "身份证号脱敏")
    public ApiResult<String> maskIdCard(@RequestParam("idCard") String idCard) {
        return ApiResult.ok(dataMaskingService.maskIdCard(idCard));
    }

    /**
     * 银行卡号脱敏
     */
    @GetMapping("/bankCard")
    @Operation(summary = "银行卡号脱敏")
    public ApiResult<String> maskBankCard(@RequestParam("bankCard") String bankCard) {
        return ApiResult.ok(dataMaskingService.maskBankCard(bankCard));
    }

    /**
     * 邮箱脱敏
     */
    @GetMapping("/email")
    @Operation(summary = "邮箱脱敏")
    public ApiResult<String> maskEmail(@RequestParam("email") String email) {
        return ApiResult.ok(dataMaskingService.maskEmail(email));
    }

    /**
     * 姓名脱敏
     */
    @GetMapping("/name")
    @Operation(summary = "姓名脱敏")
    public ApiResult<String> maskName(@RequestParam("name") String name) {
        return ApiResult.ok(dataMaskingService.maskName(name));
    }

    /**
     * 地址脱敏
     */
    @GetMapping("/address")
    @Operation(summary = "地址脱敏")
    public ApiResult<String> maskAddress(@RequestParam("address") String address) {
        return ApiResult.ok(dataMaskingService.maskAddress(address));
    }
}
