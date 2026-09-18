package com.budaos.support.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.budaos.support.domain.entity.SerialCodeRecordEntity;
import com.budaos.support.domain.form.SerialCodeRecordQueryForm;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 单据序列号 生成的记录
 *
 * @author budaos
 */
@Mapper
public interface SerialCodeRecordDao extends BaseMapper<SerialCodeRecordEntity> {

    /**
     * 根据 id和日期 查询 记录id
     *
     * @param serialNumberId
     * @param recordDate
     * @return 返回记录的ID或null
     */
    Long selectRecordIdBySerialNumberIdAndDate(@Param("serialNumberId") Integer serialNumberId,
                                               @Param("recordDate") String recordDate);

    /**
     * 更新记录（增强版）
     *
     * @param serialNumberId
     * @param recordDate
     * @param lastNumber
     * @param originalSequence
     * @param count
     * @return
     */
    Long updateRecordWithOriginal(@Param("serialNumberId") Integer serialNumberId,
                                  @Param("recordDate") LocalDate recordDate,
                                  @Param("lastNumber") Long lastNumber,
                                  @Param("originalSequence") Long originalSequence,
                                  @Param("count") int count);

    /**
     * 分页查询记录
     *
     * @param page
     * @param queryForm
     * @return
     */
    List<SerialCodeRecordEntity> query(Page page, @Param("queryForm") SerialCodeRecordQueryForm queryForm);

    /**
     * 更新记录（兼容旧版）
     */
    @Deprecated
    Long updateRecord(@Param("serialNumberId") Integer serialNumberId,
                      @Param("recordDate") LocalDate recordDate,
                      @Param("lastNumber") Long lastNumber,
                      @Param("count") int count);
}
