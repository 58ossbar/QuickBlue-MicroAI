package com.budaos.business.notice.service;

import com.budaos.business.notice.constant.DataChangeTraceTypeEnum;
import com.budaos.business.notice.domain.entity.AnnouncementEntity;
import org.springframework.stereotype.Service;

/**
 * 数据追踪服务 - 占位符
 * TODO: 后续实现完整的数据追踪功能
 */
@Service
public class DataChangeTraceService {

    public void insert(AnnouncementEntity entity, DataChangeTraceTypeEnum dataTracerTypeEnum) {
        // TODO: 实现数据追踪插入逻辑
    }

    public void update(Long noticeId, DataChangeTraceTypeEnum dataTracerTypeEnum, AnnouncementEntity old, AnnouncementEntity noticeEntity) {
        // TODO: 实现数据追踪更新逻辑
    }

    public void delete(Long noticeId, DataChangeTraceTypeEnum dataTracerTypeEnum) {
        // TODO: 实现数据追踪删除逻辑
    }
}
