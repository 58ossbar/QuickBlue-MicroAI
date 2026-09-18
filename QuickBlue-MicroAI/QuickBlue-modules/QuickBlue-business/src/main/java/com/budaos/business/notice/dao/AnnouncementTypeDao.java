package com.budaos.business.notice.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.budaos.business.notice.domain.entity.AnnouncementTypeEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 通知公告类型
 *
 */
@Mapper
public interface AnnouncementTypeDao extends BaseMapper<AnnouncementTypeEntity> {

}
