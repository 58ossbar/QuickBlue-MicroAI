package com.budaos.support.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.common.core.util.EnumValueUtil;
import com.budaos.common.web.annotation.AuditLog;
import com.budaos.support.constant.SerialCodeIdEnum;
import com.budaos.support.dao.SerialCodeDao;
import com.budaos.support.domain.entity.SerialCodeEntity;
import com.budaos.support.domain.entity.SerialCodeRecordEntity;
import com.budaos.support.domain.form.SerialCodeGenerateForm;
import com.budaos.support.domain.form.SerialCodeRecordQueryForm;
import com.budaos.support.service.SerialCodeRecordService;
import com.budaos.support.service.SerialCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 单据序列号Controller
 *
 * @author budaos
 */
@Tag(name = "单据序列号管理")
@RestController
@RequestMapping("/serialNumber")
@AuditLog(module = "单据序列号管理", description = "单据序列号操作")
public class SerialCodeController {

    @Resource
    private SerialCodeDao serialNumberDao;

    @Resource
    private SerialCodeService serialNumberService;

    @Resource
    private SerialCodeRecordService serialNumberRecordService;

    @Operation(summary = "生成单号")
    @PostMapping("/generate")
    @SaCheckPermission("support:serialNumber:generate")
    public ApiResult<List<String>> generate(@RequestBody @Valid SerialCodeGenerateForm generateForm) {
        SerialCodeIdEnum serialNumberIdEnum = EnumValueUtil.getEnumByValue(generateForm.getSerialNumberId(), SerialCodeIdEnum.class);
        if (null == serialNumberIdEnum) {
            return ApiResult.userErrorParam("SerialNumberId不存在: " + generateForm.getSerialNumberId());
        }
        return ApiResult.ok(serialNumberService.generate(serialNumberIdEnum, generateForm.getCount()));
    }

    @Operation(summary = "获取所有单号定义")
    @GetMapping("/all")
    public ApiResult<List<SerialCodeEntity>> getAllSerialNumber() {
        return ApiResult.ok(serialNumberDao.selectList(null));
    }

    @Operation(summary = "获取生成记录")
    @PostMapping("/queryRecord")
    @SaCheckPermission("support:serialNumber:record")
    public ApiResult<PageResponse<SerialCodeRecordEntity>> querySerialNumberRecord(@RequestBody @Valid SerialCodeRecordQueryForm queryForm) {
        return ApiResult.ok(serialNumberRecordService.query(queryForm));
    }

}
