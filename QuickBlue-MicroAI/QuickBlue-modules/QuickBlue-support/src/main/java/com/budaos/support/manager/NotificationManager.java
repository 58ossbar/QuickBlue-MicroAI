package com.budaos.support.manager;

import com.budaos.support.domain.entity.NotificationEntity;
import com.budaos.support.dao.NotificationDao;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 消息Manager
 *
 * @author budaos
 */
@Component
public class NotificationManager {

    @Resource
    private NotificationDao messageDao;

    /**
     * 批量保存消息
     */
    public void saveBatch(List<NotificationEntity> messageEntityList) {
        for (NotificationEntity entity : messageEntityList) {
            messageDao.insert(entity);
        }
    }
}
