package com.budaos.support.service.impl;

import com.budaos.common.core.exception.BizException;
import com.budaos.support.domain.bo.SerialCodeGenerateResultBO;
import com.budaos.support.domain.bo.SerialCodeInfoBO;
import com.budaos.support.domain.bo.SerialCodeLastGenerateBO;
import com.budaos.support.domain.entity.SerialCodeEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class SerialCodeMysqlService extends SerialCodeBaseService {

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public List<String> generateSerialNumberList(SerialCodeInfoBO serialNumberInfo, int count) {
        // Get last generate result
        SerialCodeEntity serialNumberEntity = serialNumberDao.selectForUpdate(serialNumberInfo.getSerialNumberId());
        if (serialNumberEntity == null) {
            throw new BizException("cannot found SerialNumberId database not exist:" + serialNumberInfo.getSerialNumberId());
        }
        SerialCodeLastGenerateBO lastGenerateBO = SerialCodeLastGenerateBO
                .builder()
                .lastNumber(serialNumberEntity.getLastNumber())
                .lastTime(serialNumberEntity.getLastTime())
                .serialNumberId(serialNumberEntity.getSerialNumberId())
                .build();

        // Generate (using enhanced loopNumberList)
        SerialCodeGenerateResultBO serialNumberGenerateResult = super.loopNumberList(lastGenerateBO, serialNumberInfo, count);

        // Save generation info to memory and database
        lastGenerateBO.setLastNumber(serialNumberGenerateResult.getLastNumber());
        lastGenerateBO.setLastTime(serialNumberGenerateResult.getLastTime());
        serialNumberDao.updateLastNumberAndTime(serialNumberInfo.getSerialNumberId(),
                serialNumberGenerateResult.getLastNumber(),
                serialNumberGenerateResult.getLastTime());

        // Save generation process to database
        super.saveRecord(serialNumberGenerateResult);

        return formatNumberList(serialNumberGenerateResult, serialNumberInfo);
    }

    @Override
    public void initLastGenerateData(List<SerialCodeEntity> serialNumberEntityList) {

    }
}
