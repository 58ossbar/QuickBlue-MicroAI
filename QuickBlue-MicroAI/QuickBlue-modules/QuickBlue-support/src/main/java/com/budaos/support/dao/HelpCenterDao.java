package com.budaos.support.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.budaos.support.domain.entity.HelpCenterEntity;
import com.budaos.support.domain.form.HelpCenterQueryForm;
import com.budaos.support.domain.form.HelpCenterViewRecordQueryForm;
import com.budaos.support.domain.vo.HelpCenterDetailVO;
import com.budaos.support.domain.vo.HelpCenterViewRecordVO;
import com.budaos.support.domain.vo.HelpCenterVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 帮助文档DAO
 *
 * @author budaos
 */
@Mapper
public interface HelpCenterDao extends BaseMapper<HelpCenterEntity> {

    /**
     * 查询全部帮助文档
     */
    List<HelpCenterVO> queryAllHelpDocList();

    /**
     * 查询全部帮助文档详情
     */
    List<HelpCenterDetailVO> queryAllHelpDocDetailList();

    /**
     * 后管分页查询帮助文档
     */
    List<HelpCenterVO> query(Page<?> page, @Param("query") HelpCenterQueryForm queryForm);

    /**
     * 更新阅读量
     */
    void updateViewCount(@Param("helpDocId") Long helpDocId,
                         @Param("userViewCountIncrease") Integer userViewCountIncrease,
                         @Param("pageViewCountIncrease") Integer pageViewCountIncrease);

    /**
     * 根据目录，查询文档
     */
    List<HelpCenterVO> queryHelpDocByCatalogId(@Param("helpDocCatalogId") Long helpDocCatalogId);

    /**
     * 根据关联文档id，查询文档
     */
    List<HelpCenterVO> queryHelpDocByRelationId(@Param("relationId") Long relationId);

    /**
     * 查询浏览记录数量
     */
    Long viewRecordCount(@Param("helpDocId") Long helpDocId, @Param("userId") Long userId);

    /**
     * 插入浏览记录
     */
    void insertViewRecord(@Param("helpDocId") Long helpDocId, @Param("userId") Long userId,
                          @Param("userName") String userName, @Param("ip") String ip,
                          @Param("userAgent") String userAgent, @Param("viewCount") Integer viewCount);

    /**
     * 更新浏览记录
     */
    void updateViewRecord(@Param("helpDocId") Long helpDocId, @Param("userId") Long userId,
                         @Param("ip") String ip, @Param("userAgent") String userAgent);

    /**
     * 分页查询浏览记录
     */
    List<HelpCenterViewRecordVO> queryViewRecordList(Page<?> page, @Param("query") HelpCenterViewRecordQueryForm queryForm);
}
