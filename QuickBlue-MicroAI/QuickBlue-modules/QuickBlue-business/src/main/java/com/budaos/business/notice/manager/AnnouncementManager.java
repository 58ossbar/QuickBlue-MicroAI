package com.budaos.business.notice.manager;


import com.budaos.business.notice.constant.DataChangeTraceTypeEnum;
import com.budaos.business.notice.dao.AnnouncementDao;
import com.budaos.business.notice.domain.entity.AnnouncementEntity;
import com.budaos.business.notice.domain.form.AnnouncementVisibleRangeForm;
import com.budaos.business.notice.service.DataChangeTraceService;
import jakarta.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 通知、公告 manager
 *
 */
@Service
public class AnnouncementManager {

    @Resource
    private AnnouncementDao noticeDao;

    @Resource
    private DataChangeTraceService dataTracerService;

    /**
     * 保存
     */
    @Transactional(rollbackFor = Throwable.class)
    public void save(AnnouncementEntity noticeEntity, List<AnnouncementVisibleRangeForm> visibleRangeFormList) {
        noticeDao.insert(noticeEntity);
        Long noticeId = noticeEntity.getNoticeId();
        // 保存可见范围
        if (CollectionUtils.isNotEmpty(visibleRangeFormList)) {
            noticeDao.insertVisibleRange(noticeId, visibleRangeFormList);
        }
        dataTracerService.insert(noticeEntity, DataChangeTraceTypeEnum.OA_NOTICE);
    }

    /**
     * 更新
     *
     */
    @Transactional(rollbackFor = Throwable.class)
    public void update(AnnouncementEntity old, AnnouncementEntity noticeEntity, List<AnnouncementVisibleRangeForm> visibleRangeList) {
        noticeDao.updateById(noticeEntity);
        Long noticeId = noticeEntity.getNoticeId();
        // 保存可见范围
        if (CollectionUtils.isNotEmpty(visibleRangeList)) {
            noticeDao.deleteVisibleRange(noticeId);
            noticeDao.insertVisibleRange(noticeId, visibleRangeList);
        }
        dataTracerService.update(noticeId, DataChangeTraceTypeEnum.OA_NOTICE, old, noticeEntity);
    }
}
