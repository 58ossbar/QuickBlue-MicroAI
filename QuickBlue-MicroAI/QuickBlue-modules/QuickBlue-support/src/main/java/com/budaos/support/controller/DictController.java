package com.budaos.support.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.common.core.domain.AutoValidateList;
import com.budaos.common.web.annotation.AuditLog;
import com.budaos.support.domain.form.*;
import com.budaos.support.domain.vo.DictionaryDataVO;
import com.budaos.support.domain.vo.DictTreeVO;
import com.budaos.support.domain.vo.DictionaryVO;
import com.budaos.support.service.DictionaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据字典Controller
 *
 * @author budaos
 */
@Tag(name = "数据字典管理")
@RestController
@RequestMapping("/dict")
@AuditLog(module = "数据字典管理", description = "数据字典操作")
public class DictController {

    @Resource
    private DictionaryService dictService;

    // -------------------  获取全部数据 -------------------

    @Operation(summary = "获取全部数据（供前端缓存使用）")
    @GetMapping("/getAllDictData")
    public ApiResult<List<DictionaryDataVO>> getAllDictData() {
        return ApiResult.ok(dictService.getAll());
    }

    @Operation(summary = "获取所有字典code")
    @GetMapping("/getAllDict")
    public ApiResult<List<DictionaryVO>> getAllDict() {
        return ApiResult.ok(dictService.getAllDict());
    }

    // -------------------  字典 -------------------

    @Operation(summary = "分页查询")
    @PostMapping("/queryPage")
    @SaCheckPermission("support:dict:query")
    public ApiResult<PageResponse<DictionaryVO>> queryPageDict(@RequestBody @Valid DictionaryQueryForm queryForm) {
        return ApiResult.ok(dictService.queryPage(queryForm));
    }

    @Operation(summary = "添加")
    @PostMapping("/add")
    @SaCheckPermission("support:dict:add")
    public ApiResult<String> addDict(@RequestBody @Valid DictionaryAddForm addForm) {
        return dictService.add(addForm);
    }

    @Operation(summary = "更新")
    @PostMapping("/update")
    @SaCheckPermission("support:dict:update")
    public ApiResult<String> updateDict(@RequestBody @Valid DictionaryUpdateForm updateForm) {
        return dictService.update(updateForm);
    }

    @Operation(summary = "启用/禁用")
    @GetMapping("/updateDisabled/{dictId}")
    @SaCheckPermission("support:dict:updateDisabled")
    public ApiResult<String> updateDisabled(@PathVariable Long dictId) {
        return dictService.updateDisabled(dictId);
    }

    @Operation(summary = "批量删除")
    @PostMapping("/batchDelete")
    @SaCheckPermission("support:dict:delete")
    public ApiResult<String> batchDeleteDict(@RequestBody AutoValidateList<Long> idList) {
        return dictService.batchDelete(idList);
    }

    @Operation(summary = "单个删除")
    @GetMapping("/delete/{dictId}")
    @SaCheckPermission("support:dict:delete")
    public ApiResult<String> delete(@PathVariable Long dictId) {
        return dictService.delete(dictId);
    }

    // -------------------  字典数据 -------------------

    @Operation(summary = "字典数据 分页查询")
    @GetMapping("/dictData/queryDictData/{dictId}")
    @SaCheckPermission("support:dictData:query")
    public ApiResult<List<DictionaryDataVO>> queryDictData(@PathVariable Long dictId) {
        return ApiResult.ok(dictService.queryDictData(dictId));
    }

    @Operation(summary = "字典数据 启用/禁用")
    @GetMapping("/dictData/updateDisabled/{dictDataId}")
    @SaCheckPermission("support:dictData:updateDisabled")
    public ApiResult<String> updateDictDataDisabled(@PathVariable Long dictDataId) {
        return dictService.updateDictDataDisabled(dictDataId);
    }

    @Operation(summary = "字典数据 添加")
    @PostMapping("/dictData/add")
    @SaCheckPermission("support:dictData:add")
    public ApiResult<String> addDictData(@RequestBody @Valid DictionaryDataAddForm addForm) {
        return dictService.addDictData(addForm);
    }

    @Operation(summary = "字典数据 更新")
    @PostMapping("/dictData/update")
    @SaCheckPermission("support:dictData:update")
    public ApiResult<String> updateDictData(@RequestBody @Valid DictionaryDataUpdateForm updateForm) {
        return dictService.updateDictData(updateForm);
    }

    @Operation(summary = "字典数据 批量删除")
    @PostMapping("/dictData/batchDelete")
    @SaCheckPermission("support:dictData:delete")
    public ApiResult<String> batchDeleteDictData(@RequestBody AutoValidateList<Long> idList) {
        return dictService.batchDeleteDictData(idList);
    }

    @Operation(summary = "字典数据 单个删除")
    @GetMapping("/dictData/delete/{dictDataId}")
    @SaCheckPermission("support:dictData:delete")
    public ApiResult<String> deleteDictData(@PathVariable Long dictDataId) {
        return dictService.deleteDictData(dictDataId);
    }

    // -------------------  字典增强功能 -------------------

    @Operation(summary = "加载字典（支持关键词搜索）")
    @GetMapping("/loadDict")
    public ApiResult<List<DictionaryDataVO>> loadDict(@RequestParam String dictCode, @RequestParam(required = false) String keyword) {
        return dictService.loadDict(dictCode, keyword);
    }

    @Operation(summary = "加载表字典（动态从业务表查询）")
    @GetMapping("/loadTableDict")
    @SaCheckPermission("support:dict:loadTableDict")
    public ApiResult<List<DictionaryDataVO>> loadTableDict(
            @RequestParam String tableName,
            @RequestParam String keyField,
            @RequestParam String labelField,
            @RequestParam(required = false) String condition) {
        return dictService.loadTableDict(tableName, keyField, labelField, condition);
    }

    @Operation(summary = "加载树形字典")
    @GetMapping("/loadTreeDict/{dictCode}")
    public ApiResult<List<DictTreeVO>> loadTreeDict(@PathVariable String dictCode) {
        return dictService.loadTreeDict(dictCode);
    }

    @Operation(summary = "异步加载字典（大数据量）")
    @GetMapping("/loadDictAsync")
    public ApiResult<List<DictionaryDataVO>> loadDictAsync(
            @RequestParam String dictCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(defaultValue = "1") Integer pageNum) {
        return dictService.loadDictAsync(dictCode, keyword, pageSize, pageNum);
    }

    @Operation(summary = "获取所有字典数据（按dictCode分组）")
    @GetMapping("/getDictMapByCode")
    public ApiResult<Map<String, List<DictionaryDataVO>>> getDictMapByCode() {
        return dictService.getDictMapByCode();
    }

}
