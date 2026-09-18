package com.budaos.support.manager;

import com.budaos.common.core.constant.CacheKeyConstants;
import com.budaos.common.core.util.BeanCopyUtil;
import com.budaos.support.dao.DictionaryDao;
import com.budaos.support.dao.DictionaryDataDao;
import com.budaos.support.domain.entity.DictionaryDataEntity;
import com.budaos.support.domain.entity.DictionaryEntity;
import com.budaos.support.domain.vo.DictionaryDataVO;
import com.budaos.common.core.util.BeanCopyUtil;
import jakarta.annotation.Resource;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 * 数据字典缓存管理器
 *
 * @author budaos
 */
@Component
public class DictionaryManager {

    @Resource
    private DictionaryDao dictDao;

    @Resource
    private DictionaryDataDao dictDataDao;

    /**
     * 获取字典
     */
    @Cacheable(value = CacheKeyConstants.Dict.DICT_DATA, key = "#dictCode + '_' + #dataValue")
    public DictionaryDataVO getDictData(String dictCode, String dataValue) {
        DictionaryEntity dictEntity = dictDao.selectByCode(dictCode);
        if (dictEntity == null) {
            return null;
        }

        DictionaryDataEntity dictDataEntity = dictDataDao.selectByDictIdAndValue(dictEntity.getDictId(), dataValue);
        return BeanCopyUtil.copyProperties(dictDataEntity, DictionaryDataVO.class);
    }

}
