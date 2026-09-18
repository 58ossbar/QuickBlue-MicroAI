package com.budaos.support.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.common.web.domain.message.NotificationQueryForm;
import com.budaos.common.web.domain.message.NotificationSendForm;
import com.budaos.common.web.domain.message.NotificationVO;
import com.budaos.support.dao.NotificationDao;
import com.budaos.support.domain.entity.NotificationEntity;
import com.budaos.support.manager.NotificationManager;
import com.budaos.support.service.NotificationService;
import com.budaos.common.core.util.BeanCopyUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 消息服务实现
 *
 * @author budaos
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    @Resource
    private NotificationDao messageDao;

    @Resource
    private NotificationManager messageManager;

    /**
     * 分页查询消息
     */
    @Override
    public PageResponse<NotificationVO> query(NotificationQueryForm queryForm) {
        Page page = new Page<>(queryForm.getPageNum(), queryForm.getPageSize());
        List<NotificationVO> messageVOList = messageDao.query(page, queryForm);
        PageResponse<NotificationVO> result = new PageResponse<>();
        result.setPageNum(page.getCurrent());
        result.setPageSize(page.getSize());
        result.setTotal(page.getTotal());
        result.setList(messageVOList);
        return result;
    }

    /**
     * 查询未读消息数量
     */
    @Override
    public Long getUnreadCount(Integer receiverUserType, Long receiverUserId) {
        return messageDao.getUnreadCount(receiverUserType, receiverUserId);
    }

    /**
     * 更新已读状态
     */
    @Override
    public void updateReadFlag(Long messageId, Integer receiverUserType, Long receiverUserId) {
        messageDao.updateReadFlag(messageId, receiverUserType, receiverUserId, true);
    }

    /**
     * 发送消息
     */
    @Override
    public void sendMessage(NotificationSendForm... sendForms) {
        this.sendMessage(List.of(sendForms));
    }

    /**
     * 批量发送通知消息
     */
    @Override
    public void sendMessage(List<NotificationSendForm> sendList) {
        for (NotificationSendForm sendDTO : sendList) {
            String verify = BeanCopyUtil.verify(sendDTO);
            if (verify != null) {
                throw new RuntimeException("send msg error: " + verify);
            }
        }
        List<NotificationEntity> messageEntityList = sendList.stream().map(e -> {
            NotificationEntity messageEntity = new NotificationEntity();
            messageEntity.setMessageType(e.getMessageType());
            messageEntity.setReceiverUserType(e.getReceiverUserType());
            messageEntity.setReceiverUserId(e.getReceiverUserId());
            messageEntity.setDataId(e.getDataId() != null ? String.valueOf(e.getDataId()) : null);
            messageEntity.setTitle(e.getTitle());
            messageEntity.setContent(e.getContent());
            return messageEntity;
        }).collect(Collectors.toList());
        messageManager.saveBatch(messageEntityList);
    }

    /**
     * 删除消息
     */
    @Override
    public ApiResult<String> delete(Long messageId) {
        if (messageId == null) {
            return ApiResult.userErrorParam();
        }
        messageDao.deleteById(messageId);
        return ApiResult.ok();
    }
}
