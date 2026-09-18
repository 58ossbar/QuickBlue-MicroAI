package com.budaos.support.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.budaos.support.domain.entity.NotificationEntity;
import com.budaos.common.web.domain.message.NotificationQueryForm;
import com.budaos.common.web.domain.message.NotificationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 消息DAO
 *
 * @author budaos
 */
@Mapper
public interface NotificationDao extends BaseMapper<NotificationEntity> {

    /**
     * 分页查询消息
     */
    List<NotificationVO> query(Page<?> page, @Param("query") NotificationQueryForm queryForm);

    /**
     * 更新已读状态
     */
    Integer updateReadFlag(@Param("messageId") Long messageId,
                           @Param("receiverUserType") Integer receiverUserType,
                           @Param("receiverUserId") Long receiverUserId,
                           @Param("readFlag") Boolean readFlag);

    /**
     * 查询未读消息数
     */
    Long getUnreadCount(@Param("receiverUserType") Integer receiverUserType,
                        @Param("receiverUserId") Long receiverUserId);
}
