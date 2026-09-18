package com.budaos.support.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.budaos.support.domain.entity.ConfigRefreshResultEntity;
import com.budaos.support.domain.vo.ConfigRefreshResultVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * t_reload_result 数据表dao
 */
@Mapper
public interface ConfigRefreshResultDao extends BaseMapper<ConfigRefreshResultEntity> {

    List<ConfigRefreshResultVO> query(@Param("tag") String tag);
}
