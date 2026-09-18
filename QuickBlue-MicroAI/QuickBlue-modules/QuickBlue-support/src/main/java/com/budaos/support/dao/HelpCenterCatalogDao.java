package com.budaos.support.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.budaos.support.domain.entity.HelpCenterCatalogEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

/**
 * 帮助文档目录 DAO
 *
 * @author budaos
 */
@Mapper
public interface HelpCenterCatalogDao extends BaseMapper<HelpCenterCatalogEntity> {

}
