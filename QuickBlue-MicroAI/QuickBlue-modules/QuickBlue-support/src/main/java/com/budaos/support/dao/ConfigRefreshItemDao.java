package com.budaos.support.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.budaos.support.domain.entity.ConfigRefreshItemEntity;
import com.budaos.support.domain.vo.ConfigRefreshItemVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * t_reload_item 数据表dao
 */
@Mapper
public interface ConfigRefreshItemDao extends BaseMapper<ConfigRefreshItemEntity> {

    List<ConfigRefreshItemVO> query();
}
