package com.budaos.support.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.budaos.support.domain.entity.SerialCodeEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * 单据序列号
 *
 * @author budaos
 */
@Mapper
public interface SerialCodeDao extends BaseMapper<SerialCodeEntity> {

    /**
     * 排他锁查询
     *
     * @param serialNumberId
     * @return
     */
    SerialCodeEntity selectForUpdate(@Param("serialNumberId") Integer serialNumberId);

    /**
     * 更新上一次的 数值和时间
     *
     * @param serialNumberId
     * @param lastNumber
     * @param lastTime
     */
    void updateLastNumberAndTime(@Param("serialNumberId") Integer serialNumberId, @Param("lastNumber") Long lastNumber, @Param("lastTime") LocalDateTime lastTime);

}
