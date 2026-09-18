package com.budaos.support.core;

import com.budaos.support.dao.KeepAliveRecordDao;
import com.budaos.support.domain.entity.KeepAliveRecordEntity;
import com.budaos.common.core.util.BeanCopyUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 心跳记录处理器
 *
 * @author budaos
 */
@Slf4j
@Service
public class KeepAliveRecordHandler implements IKeepAliveRecordHandler {

    @Resource
    private KeepAliveRecordDao heartBeatRecordDao;

    /**
     * 心跳日志处理方法
     *
     * @param heartBeatRecord 心跳记录
     */
    @Override
    public void handler(KeepAliveRecord heartBeatRecord) {
        KeepAliveRecordEntity heartBeatRecordEntity = BeanCopyUtil.copyProperties(heartBeatRecord, KeepAliveRecordEntity.class);
        KeepAliveRecordEntity heartBeatRecordOld = heartBeatRecordDao.query(heartBeatRecordEntity);
        if (heartBeatRecordOld == null) {
            heartBeatRecordDao.insert(heartBeatRecordEntity);
        } else {
            heartBeatRecordDao.updateHeartBeatTimeById(heartBeatRecordOld.getHeartBeatRecordId(), heartBeatRecordEntity.getHeartBeatTime());
        }
    }
}
