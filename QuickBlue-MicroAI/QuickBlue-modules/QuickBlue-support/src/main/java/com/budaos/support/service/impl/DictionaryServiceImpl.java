package com.budaos.support.service.impl;

import cn.hutool.core.util.StrUtil;
import com.budaos.common.core.constant.CacheKeyConstants;
import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.support.dao.DictionaryDao;
import com.budaos.support.dao.DictionaryDataDao;
import com.budaos.support.domain.entity.DictionaryDataEntity;
import com.budaos.support.domain.entity.DictionaryEntity;
import com.budaos.support.domain.form.*;
import com.budaos.support.domain.vo.DictionaryDataVO;
import com.budaos.support.domain.vo.DictTreeVO;
import com.budaos.support.domain.vo.DictionaryVO;
import com.budaos.support.manager.DictionaryManager;
import com.budaos.support.service.DictionaryService;
import com.budaos.common.core.util.BeanCopyUtil;
import com.budaos.common.core.util.StringParseUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据字典服务实现
 *
 * @author budaos
 */
@Service
public class DictionaryServiceImpl implements DictionaryService {

    @Resource
    private DictionaryDao dictDao;

    @Resource
    private DictionaryDataDao dictDataDao;

    @Resource
    private CacheManager cacheManager;

    @Resource
    private DictionaryManager dictManager;

    @Resource
    private JdbcTemplate jdbcTemplate;

    /**
     * 初始化：加载表字典白名单
     */
    @PostConstruct
    public void init() {
        try {
            // 如果存在DictTableSecurityUtil，则初始化白名单
            Class<?> securityUtilClass = Class.forName("com.budaos.support.util.DictTableSecurityUtil");
            Object securityUtil = securityUtilClass.getDeclaredConstructor().newInstance();

            // 从数据库加载白名单
            String sql = "SELECT id, table_name, key_field, label_field, remark, status FROM t_dict_table_whitelist WHERE status = 1";
            List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);

            // 转换为白名单对象列表
            Class<?> whiteListClass = Class.forName("com.budaos.support.util.DictTableSecurityUtil$DictTableWhiteList");
            List<Object> whiteList = list.stream().map(map -> {
                try {
                    Object item = whiteListClass.getDeclaredConstructor().newInstance();
                    whiteListClass.getMethod("setId", Long.class).invoke(item, ((Number) map.get("id")).longValue());
                    whiteListClass.getMethod("setTableName", String.class).invoke(item, map.get("table_name"));
                    whiteListClass.getMethod("setKeyField", String.class).invoke(item, map.get("key_field"));
                    whiteListClass.getMethod("setLabelField", String.class).invoke(item, map.get("label_field"));
                    whiteListClass.getMethod("setRemark", String.class).invoke(item, map.get("remark"));
                    whiteListClass.getMethod("setStatus", Integer.class).invoke(item, ((Number) map.get("status")).intValue());
                    return item;
                } catch (Exception e) {
                    return null;
                }
            }).filter(Objects::nonNull).collect(Collectors.toList());

            // 设置白名单
            securityUtilClass.getMethod("setWhiteList", List.class).invoke(securityUtil, whiteList);
            System.out.println("表字典白名单初始化完成，共 " + whiteList.size() + " 条记录");
        } catch (ClassNotFoundException e) {
            // 如果安全工具类不存在，跳过初始化
            System.err.println("警告: DictTableSecurityUtil不存在，跳过表字典白名单初始化");
        } catch (Exception e) {
            System.err.println("表字典白名单初始化失败: " + e.getMessage());
        }
    }

    /**
     * 获取全部数据
     */
    @Override
    public List<DictionaryDataVO> getAll() {
        return dictDataDao.getAll();
    }

    /**
     * 获取所有字典
     */
    @Override
    public List<DictionaryVO> getAllDict() {
        List<DictionaryEntity> dictEntityList = dictDao.selectList(null)
                .stream()
                .filter(e -> !e.getDisabledFlag())
                .collect(Collectors.toList());
        return BeanCopyUtil.copyList(dictEntityList, DictionaryVO.class);
    }

    /**
     * 分页查询
     */
    @Override
    public PageResponse<DictionaryVO> queryPage(DictionaryQueryForm queryForm) {
        long offset = (queryForm.getPageNum() - 1) * queryForm.getPageSize();
        long limit = queryForm.getPageSize();
        List<DictionaryVO> list = dictDao.queryPage(
                (int) offset,
                (int) limit,
                queryForm.getKeywords(),
                queryForm.getDisabledFlag()
        );
        long total = dictDao.queryPage(
                null,
                null,
                queryForm.getKeywords(),
                queryForm.getDisabledFlag()
        ).size();
        PageResponse<DictionaryVO> result = new PageResponse<>();
        result.setPageNum(queryForm.getPageNum());
        result.setPageSize(queryForm.getPageSize());
        result.setTotal(total);
        result.setList(list);
        return result;
    }

    /**
     * 添加
     */
    @Override
    public ApiResult<String> add(DictionaryAddForm addForm) {
        DictionaryEntity existDictCode = dictDao.selectByCode(addForm.getDictCode());
        if (null != existDictCode) {
            return ApiResult.userErrorParam("数据字典编码已经存在！");
        }

        DictionaryEntity dictEntity = BeanCopyUtil.copyProperties(addForm, DictionaryEntity.class);
        dictEntity.setDisabledFlag(false);
        dictDao.insert(dictEntity);
        return ApiResult.ok();
    }

    /**
     * 禁用/启用
     */
    @Override
    public ApiResult<String> updateDisabled(Long dictId) {
        DictionaryEntity dictEntity = dictDao.selectById(dictId);
        if (dictEntity == null) {
            return ApiResult.userErrorParam("数据不存在");
        }

        dictEntity.setDisabledFlag(!dictEntity.getDisabledFlag());
        dictDao.updateById(dictEntity);
        return ApiResult.ok();
    }

    /**
     * 更新
     */
    @Override
    @CacheEvict(CacheKeyConstants.Dict.DICT_DATA)
    public synchronized ApiResult<String> update(DictionaryUpdateForm updateForm) {
        DictionaryEntity existDictCode = dictDao.selectByCode(updateForm.getDictCode());
        if (null != existDictCode && !existDictCode.getDictId().equals(updateForm.getDictId())) {
            return ApiResult.userErrorParam("数据字典编码已经存在！");
        }

        DictionaryEntity dictEntity = BeanCopyUtil.copyProperties(updateForm, DictionaryEntity.class);
        dictDao.updateById(dictEntity);
        return ApiResult.ok();
    }

    /**
     * 批量删除
     */
    @Override
    @CacheEvict(CacheKeyConstants.Dict.DICT_DATA)
    public synchronized ApiResult<String> batchDelete(List<Long> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return ApiResult.ok();
        }

        dictDao.deleteBatchIds(idList);
        return ApiResult.ok();
    }

    /**
     * 单个删除
     */
    @Override
    @CacheEvict(CacheKeyConstants.Dict.DICT_DATA)
    public synchronized ApiResult<String> delete(Long dictId) {
        if (null == dictId) {
            return ApiResult.ok();
        }

        dictDao.deleteById(dictId);
        return ApiResult.ok();
    }

    /**
     * 分页查询字典数据
     */
    @Override
    public List<DictionaryDataVO> queryDictData(Long dictId) {
        return dictDataDao.queryByDictId(dictId);
    }

    /**
     * 获取字典
     */
    @Override
    public DictionaryDataVO getDictData(String dictCode, String dataValue) {
        return dictManager.getDictData(dictCode, dataValue);
    }

    /**
     * 获取字典Label
     */
    @Override
    public String getDictDataLabel(String dictCode, String dataValue) {
        DictionaryDataVO dictData = getDictData(dictCode, dataValue);
        return dictData == null ? "" : dictData.getDataLabel();
    }

    /**
     * 添加字典数据
     */
    @Override
    public synchronized ApiResult<String> addDictData(DictionaryDataAddForm addForm) {
        addForm.setDataValue(StringParseUtil.trim(addForm.getDataValue()));

        DictionaryEntity dictEntity = dictDao.selectById(addForm.getDictId());
        if (null == dictEntity) {
            return ApiResult.userErrorParam("数据字典不存在");
        }

        DictionaryDataEntity existData = dictDataDao.selectByDictIdAndValue(addForm.getDictId(), addForm.getDataValue());
        if (null != existData) {
            return ApiResult.userErrorParam("已存在相同value的数据");
        }

        DictionaryDataEntity dictDataEntity = BeanCopyUtil.copyProperties(addForm, DictionaryDataEntity.class);
        dictDataDao.insert(dictDataEntity);
        return ApiResult.ok();
    }

    /**
     * 更新字典数据
     */
    @Override
    @CacheEvict(value = CacheKeyConstants.Dict.DICT_DATA, key = "#updateForm.dictCode + '_' + #updateForm.dataValue")
    public synchronized ApiResult<String> updateDictData(DictionaryDataUpdateForm updateForm) {
        updateForm.setDataValue(StringParseUtil.trim(updateForm.getDataValue()));

        DictionaryEntity dictEntity = dictDao.selectById(updateForm.getDictId());
        if (null == dictEntity) {
            return ApiResult.userErrorParam("数据字典不存在");
        }

        DictionaryDataEntity existData = dictDataDao.selectByDictIdAndValue(updateForm.getDictId(), updateForm.getDataValue());
        if (null != existData && !existData.getDictDataId().equals(updateForm.getDictDataId())) {
            return ApiResult.userErrorParam("已存在相同value的数据");
        }

        DictionaryDataEntity dictDataEntity = BeanCopyUtil.copyProperties(updateForm, DictionaryDataEntity.class);
        dictDataDao.updateById(dictDataEntity);
        return ApiResult.ok();
    }

    /**
     * 批量删除字典数据
     */
    @Override
    public synchronized ApiResult<String> batchDeleteDictData(List<Long> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return ApiResult.ok();
        }
        // 清除缓存
        clearDictDataCache(idList);
        // 删除
        dictDataDao.deleteBatchIds(idList);
        return ApiResult.ok();
    }

    /**
     * 单个删除字典数据
     */
    @Override
    public synchronized ApiResult<String> deleteDictData(Long dictDataId) {
        if (null == dictDataId) {
            return ApiResult.ok();
        }
        // 清除缓存
        clearDictDataCache(Collections.singletonList(dictDataId));
        // 删除
        dictDataDao.deleteById(dictDataId);
        return ApiResult.ok();
    }

    /**
     * 更新启用/禁用
     */
    @Override
    public synchronized ApiResult<String> updateDictDataDisabled(Long dictDataId) {
        DictionaryDataEntity dictDataEntity = dictDataDao.selectById(dictDataId);
        if (dictDataEntity == null) {
            return ApiResult.userErrorParam("数据不存在");
        }

        dictDataEntity.setDisabledFlag(!dictDataEntity.getDisabledFlag());
        dictDataDao.updateById(dictDataEntity);
        return ApiResult.ok();
    }

    /**
     * 清空字典数据缓存
     */
    private void clearDictDataCache(List<Long> idList) {
        List<DictionaryDataVO> dictDataList = dictDataDao.selectByDictDataIds(idList);
        Cache cache = cacheManager.getCache(CacheKeyConstants.Dict.DICT_DATA);
        if (cache == null) {
            return;
        }

        for (DictionaryDataVO dictDataVO : dictDataList) {
            cache.evict(dictDataVO.getDictCode() + "_" + dictDataVO.getDataValue());
        }
    }

    // -------------------  字典增强功能 -------------------

    /**
     * 加载字典（支持关键词搜索）
     */
    @Override
    public ApiResult<List<DictionaryDataVO>> loadDict(String dictCode, String keyword) {
        // 1. 查询字典
        DictionaryEntity dictEntity = dictDao.selectByCode(dictCode);
        if (dictEntity == null) {
            return ApiResult.userErrorParam("字典不存在: " + dictCode);
        }

        // 2. 查询字典数据
        List<DictionaryDataVO> dictDataList = dictDataDao.queryByDictId(dictEntity.getDictId());

        // 3. 过滤关键词
        if (StrUtil.isNotBlank(keyword)) {
            final String lowerKeyword = keyword.toLowerCase();
            dictDataList = dictDataList.stream()
                    .filter(item -> {
                        boolean matchValue = item.getDataValue() != null && item.getDataValue().toLowerCase().contains(lowerKeyword);
                        boolean matchLabel = item.getDataLabel() != null && item.getDataLabel().toLowerCase().contains(lowerKeyword);
                        return matchValue || matchLabel;
                    })
                    .collect(Collectors.toList());
        }

        return ApiResult.ok(dictDataList);
    }

    /**
     * 加载表字典（动态从业务表查询）
     */
    @Override
    public ApiResult<List<DictionaryDataVO>> loadTableDict(String tableName, String keyField, String labelField, String condition) {
        // 1. 安全校验 - 使用DictTableSecurityUtil进行SQL注入防护
        try {
            Class<?> securityUtilClass = Class.forName("com.budaos.support.util.DictTableSecurityUtil");
            Object securityUtil = securityUtilClass.getDeclaredConstructor().newInstance();
            java.lang.reflect.Method validateMethod = securityUtilClass.getMethod("validate", 
                String.class, String.class, String.class, String.class);
            String error = (String) validateMethod.invoke(securityUtil, tableName, keyField, labelField, condition);
            if (error != null) {
                return ApiResult.userErrorParam(error);
            }
        } catch (ClassNotFoundException e) {
            // 如果安全工具类不存在，继续执行但记录警告
            System.err.println("警告: DictTableSecurityUtil不存在，跳过表字典安全校验");
        } catch (Exception e) {
            return ApiResult.userErrorParam("表字典安全校验失败: " + e.getMessage());
        }

        // 2. 构建SQL
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ").append(keyField).append(" as data_value, ")
                .append(labelField).append(" as data_label FROM ")
                .append(tableName);

        // 3. 添加条件
        List<Object> params = new ArrayList<>();
        if (StrUtil.isNotBlank(condition)) {
            sql.append(" WHERE ").append(condition);
        }

        // 4. 查询数据
        List<DictionaryDataVO> dictDataList;
        try {
            if (params.isEmpty()) {
                List<Map<String, Object>> list = jdbcTemplate.queryForList(sql.toString());
                dictDataList = list.stream().map(map -> {
                    DictionaryDataVO vo = new DictionaryDataVO();
                    vo.setDataValue(String.valueOf(map.get("data_value")));
                    vo.setDataLabel(String.valueOf(map.get("data_label")));
                    return vo;
                }).collect(Collectors.toList());
            } else {
                List<Map<String, Object>> list = jdbcTemplate.queryForList(sql.toString(), params.toArray());
                dictDataList = list.stream().map(map -> {
                    DictionaryDataVO vo = new DictionaryDataVO();
                    vo.setDataValue(String.valueOf(map.get("data_value")));
                    vo.setDataLabel(String.valueOf(map.get("data_label")));
                    return vo;
                }).collect(Collectors.toList());
            }
        } catch (Exception e) {
            return ApiResult.userErrorParam("查询表字典失败: " + e.getMessage());
        }

        return ApiResult.ok(dictDataList);
    }

    /**
     * 加载树形字典
     */
    @Override
    public ApiResult<List<DictTreeVO>> loadTreeDict(String dictCode) {
        // 1. 查询字典
        DictionaryEntity dictEntity = dictDao.selectByCode(dictCode);
        if (dictEntity == null) {
            return ApiResult.userErrorParam("字典不存在: " + dictCode);
        }

        // 2. 查询字典数据
        List<DictionaryDataVO> dictDataList = dictDataDao.queryByDictId(dictEntity.getDictId());

        // 3. 转换为树形结构
        List<DictTreeVO> treeList = buildTree(dictDataList);

        return ApiResult.ok(treeList);
    }

    /**
     * 异步加载字典（大数据量）
     */
    @Override
    public ApiResult<List<DictionaryDataVO>> loadDictAsync(String dictCode, String keyword, Integer pageSize, Integer pageNum) {
        // 1. 查询字典
        DictionaryEntity dictEntity = dictDao.selectByCode(dictCode);
        if (dictEntity == null) {
            return ApiResult.userErrorParam("字典不存在: " + dictCode);
        }

        // 2. 分页查询字典数据
        int offset = (pageNum - 1) * pageSize;
        int limit = pageSize;
        List<DictionaryDataVO> dictDataList = dictDataDao.queryPageByDictId(dictEntity.getDictId(), offset, limit);

        // 3. 过滤关键词
        if (StrUtil.isNotBlank(keyword)) {
            final String lowerKeyword = keyword.toLowerCase();
            dictDataList = dictDataList.stream()
                    .filter(item -> {
                        boolean matchValue = item.getDataValue() != null && item.getDataValue().toLowerCase().contains(lowerKeyword);
                        boolean matchLabel = item.getDataLabel() != null && item.getDataLabel().toLowerCase().contains(lowerKeyword);
                        return matchValue || matchLabel;
                    })
                    .collect(Collectors.toList());
        }

        return ApiResult.ok(dictDataList);
    }

    /**
     * 获取所有字典数据（按dictCode分组）
     */
    @Override
    public ApiResult<Map<String, List<DictionaryDataVO>>> getDictMapByCode() {
        // 1. 查询所有字典数据
        List<DictionaryDataVO> allDictData = dictDataDao.getAll();

        // 2. 按dictCode分组
        Map<String, List<DictionaryDataVO>> dictMap = allDictData.stream()
                .collect(Collectors.groupingBy(DictionaryDataVO::getDictCode));

        return ApiResult.ok(dictMap);
    }

    /**
     * 构建树形结构
     */
    private List<DictTreeVO> buildTree(List<DictionaryDataVO> dictDataList) {
        List<DictTreeVO> treeList = new ArrayList<>();

        // 查找根节点
        for (DictionaryDataVO dictData : dictDataList) {
            if (StrUtil.isBlank(dictData.getParentCode())) {
                DictTreeVO treeVO = new DictTreeVO();
                treeVO.setDictValue(dictData.getDataValue());
                treeVO.setDictLabel(dictData.getDataLabel());
                treeVO.setDictText(dictData.getDataLabel());
                treeVO.setParentCode(dictData.getParentCode());
                treeVO.setSortOrder(dictData.getSortOrder());
                treeVO.setChildren(new ArrayList<>());

                // 递归查找子节点
                buildChildren(treeVO, dictDataList);

                treeList.add(treeVO);
            }
        }

        // 排序
        treeList.sort(Comparator.comparing(DictTreeVO::getSortOrder));

        return treeList;
    }

    /**
     * 递归构建子节点
     */
    private void buildChildren(DictTreeVO parent, List<DictionaryDataVO> dictDataList) {
        if (parent.getChildren() == null) {
            parent.setChildren(new ArrayList<>());
        }

        for (DictionaryDataVO dictData : dictDataList) {
            if (parent.getDictValue().equals(dictData.getParentCode())) {
                DictTreeVO child = new DictTreeVO();
                child.setDictValue(dictData.getDataValue());
                child.setDictLabel(dictData.getDataLabel());
                child.setDictText(dictData.getDataLabel());
                child.setParentCode(dictData.getParentCode());
                child.setSortOrder(dictData.getSortOrder());
                child.setChildren(new ArrayList<>());

                // 递归查找子节点
                buildChildren(child, dictDataList);

                parent.getChildren().add(child);
            }
        }

        // 排序
        parent.getChildren().sort(Comparator.comparing(DictTreeVO::getSortOrder));
    }

}
