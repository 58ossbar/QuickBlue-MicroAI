package com.budaos.support.service;

import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.support.domain.entity.HelpCenterEntity;
import com.budaos.support.domain.form.HelpCenterQueryForm;
import com.budaos.support.domain.vo.HelpCenterVO;

/**
 * 帮助文档服务
 *
 * @author budaos
 */
public interface HelpCenterService {

    /**
     * 分页查询帮助文档
     *
     * @param queryForm 查询表单
     * @return 分页结果
     */
    PageResponse<HelpCenterVO> query(HelpCenterQueryForm queryForm);

    /**
     * 根据ID获取帮助文档
     *
     * @param helpDocId 文档ID
     * @return 文档实体
     */
    HelpCenterEntity getById(Long helpDocId);

    /**
     * 根据ID获取帮助文档详情（包含完整信息）
     *
     * @param helpDocId 文档ID
     * @return 文档VO
     */
    com.budaos.support.domain.vo.HelpCenterDetailVO getDetail(Long helpDocId);

    /**
     * 添加帮助文档
     *
     * @param entity 文档实体
     * @return 添加结果
     */
    ApiResult<String> add(HelpCenterEntity entity);

    /**
     * 更新帮助文档
     *
     * @param entity 文档实体
     * @return 更新结果
     */
    ApiResult<String> update(HelpCenterEntity entity);

    /**
     * 删除帮助文档
     *
     * @param helpDocId 文档ID
     * @return 删除结果
     */
    ApiResult<String> delete(Long helpDocId);

    /**
     * 更新阅读量
     *
     * @param helpDocId 文档ID
     * @param userViewCountIncrease 用户浏览增量
     * @param pageViewCountIncrease 页面浏览增量
     */
    void updateViewCount(Long helpDocId, Integer userViewCountIncrease, Integer pageViewCountIncrease);

    /**
     * 根据目录查询文档
     *
     * @param helpDocCatalogId 目录ID
     * @return 文档列表
     */
    java.util.List<HelpCenterVO> queryByCatalogId(Long helpDocCatalogId);

    /**
     * 根据关联文档id，查询文档
     *
     * @param relationId 关联ID
     * @return 文档列表
     */
    java.util.List<HelpCenterVO> queryByRelationId(Long relationId);
}
