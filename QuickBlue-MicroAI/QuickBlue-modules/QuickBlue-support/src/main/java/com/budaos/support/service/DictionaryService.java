package com.budaos.support.service;

import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.support.domain.form.*;
import com.budaos.support.domain.vo.DictionaryDataVO;
import com.budaos.support.domain.vo.DictTreeVO;
import com.budaos.support.domain.vo.DictionaryVO;

import java.util.List;
import java.util.Map;

/**
 * 字典服务接口
 *
 * @author budaos
 */
public interface DictionaryService {

    /**
     * 获取全部数据
     */
    List<DictionaryDataVO> getAll();

    /**
     * 获取所有字典
     */
    List<DictionaryVO> getAllDict();

    /**
     * 分页查询
     */
    PageResponse<DictionaryVO> queryPage(DictionaryQueryForm queryForm);

    /**
     * 添加
     */
    ApiResult<String> add(DictionaryAddForm addForm);

    /**
     * 禁用/启用
     */
    ApiResult<String> updateDisabled(Long dictId);

    /**
     * 更新
     */
    ApiResult<String> update(DictionaryUpdateForm updateForm);

    /**
     * 批量删除
     */
    ApiResult<String> batchDelete(List<Long> idList);

    /**
     * 单个删除
     */
    ApiResult<String> delete(Long dictId);

    /**
     * 分页查询字典数据
     */
    List<DictionaryDataVO> queryDictData(Long dictId);

    /**
     * 获取字典
     */
    DictionaryDataVO getDictData(String dictCode, String dataValue);

    /**
     * 获取字典Label
     */
    String getDictDataLabel(String dictCode, String dataValue);

    /**
     * 添加字典数据
     */
    ApiResult<String> addDictData(DictionaryDataAddForm addForm);

    /**
     * 更新字典数据
     */
    ApiResult<String> updateDictData(DictionaryDataUpdateForm updateForm);

    /**
     * 批量删除字典数据
     */
    ApiResult<String> batchDeleteDictData(List<Long> idList);

    /**
     * 单个删除字典数据
     */
    ApiResult<String> deleteDictData(Long dictDataId);

    /**
     * 更新启用/禁用
     */
    ApiResult<String> updateDictDataDisabled(Long dictDataId);

    // -------------------  字典增强功能 -------------------

    /**
     * 加载字典（支持关键词搜索）
     */
    ApiResult<List<DictionaryDataVO>> loadDict(String dictCode, String keyword);

    /**
     * 加载表字典（动态从业务表查询）
     */
    ApiResult<List<DictionaryDataVO>> loadTableDict(String tableName, String keyField, String labelField, String condition);

    /**
     * 加载树形字典
     */
    ApiResult<List<DictTreeVO>> loadTreeDict(String dictCode);

    /**
     * 异步加载字典（大数据量）
     */
    ApiResult<List<DictionaryDataVO>> loadDictAsync(String dictCode, String keyword, Integer pageSize, Integer pageNum);

    /**
     * 获取所有字典数据（按dictCode分组）
     */
    ApiResult<Map<String, List<DictionaryDataVO>>> getDictMapByCode();

}
