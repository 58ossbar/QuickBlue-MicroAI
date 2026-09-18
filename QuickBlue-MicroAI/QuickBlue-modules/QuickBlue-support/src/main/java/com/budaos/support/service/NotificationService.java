package com.budaos.support.service;

import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.common.web.domain.message.NotificationQueryForm;
import com.budaos.common.web.domain.message.NotificationSendForm;
import com.budaos.common.web.domain.message.NotificationVO;

import java.util.List;

/**
 * 消息服务接口
 *
 * @author budaos
 */
public interface NotificationService {

    /**
     * 分页查询消息
     */
    PageResponse<NotificationVO> query(NotificationQueryForm queryForm);

    /**
     * 查询未读消息数量
     */
    Long getUnreadCount(Integer receiverUserType, Long receiverUserId);

    /**
     * 更新已读状态
     */
    void updateReadFlag(Long messageId, Integer receiverUserType, Long receiverUserId);

    /**
     * 发送消息
     */
    void sendMessage(NotificationSendForm... sendForms);

    /**
     * 批量发送通知消息
     */
    void sendMessage(List<NotificationSendForm> sendList);

    /**
     * 删除消息
     */
    ApiResult<String> delete(Long messageId);
}
