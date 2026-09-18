package com.budaos.support.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.budaos.support.domain.entity.DictionaryEntity;
import com.budaos.support.domain.vo.DictionaryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 数据字典DAO
 *
 * @author budaos
 */
@Mapper
public interface DictionaryDao extends BaseMapper<DictionaryEntity> {

    /**
     * 分页查询
     */
    List<DictionaryVO> queryPage(@Param("offset") Integer offset,
                           @Param("limit") Integer limit,
                           @Param("keywords") String keywords,
                           @Param("disabledFlag") Boolean disabledFlag);

    /**
     * 根据dictCode查询
     */
    DictionaryEntity selectByCode(@Param("code") String code);

}
