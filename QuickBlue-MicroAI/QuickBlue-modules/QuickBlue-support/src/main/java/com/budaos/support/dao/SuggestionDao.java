package com.budaos.support.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.budaos.support.domain.entity.SuggestionEntity;
import com.budaos.support.domain.form.SuggestionQueryForm;
import com.budaos.support.domain.vo.SuggestionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 意见反馈DAO
 *
 * @author budaos
 */
@Mapper
public interface SuggestionDao extends BaseMapper<SuggestionEntity> {

    /**
     * 分页查询
     */
    List<SuggestionVO> queryPage(Page page, @Param("query") SuggestionQueryForm query);
}
