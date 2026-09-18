package com.budaos.support.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.budaos.support.domain.entity.DictionaryDataEntity;
import com.budaos.support.domain.vo.DictionaryDataVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * 字典数据表DAO
 *
 * @author budaos
 */
@Mapper
public interface DictionaryDataDao extends BaseMapper<DictionaryDataEntity> {

    List<DictionaryDataVO> queryByDictId(@Param("dictId") Long dictId);

    List<DictionaryDataVO> selectByDictDataIds(@Param("dictDataIdList") Collection<Long> dictDataIds);

    DictionaryDataEntity selectByDictIdAndValue(@Param("dictId") Long dictId, @Param("dataValue") String dataValue);

    List<DictionaryDataVO> getAll();

    /**
     * 分页查询字典数据
     */
    List<DictionaryDataVO> queryPageByDictId(@Param("dictId") Long dictId, @Param("offset") Integer offset, @Param("limit") Integer limit);

}
