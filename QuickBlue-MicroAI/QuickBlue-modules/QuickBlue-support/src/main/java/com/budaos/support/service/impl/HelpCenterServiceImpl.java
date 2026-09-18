package com.budaos.support.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.support.dao.HelpCenterDao;
import com.budaos.support.domain.entity.HelpCenterEntity;
import com.budaos.support.domain.form.HelpCenterQueryForm;
import com.budaos.support.domain.vo.HelpCenterVO;
import com.budaos.support.service.HelpCenterService;
import com.budaos.common.core.util.PageConvertUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 帮助文档服务实现
 *
 * @author budaos
 */
@Slf4j
@Service
public class HelpCenterServiceImpl extends ServiceImpl<HelpCenterDao, HelpCenterEntity> implements HelpCenterService {

    @Resource
    private HelpCenterDao helpDocDao;

    @Override
    public PageResponse<HelpCenterVO> query(HelpCenterQueryForm queryForm) {
        Page<?> page = PageConvertUtil.convert2PageQuery(queryForm);
        List<HelpCenterVO> list = helpDocDao.query(page, queryForm);
        return PageConvertUtil.convert2PageResult(page, list);
    }

    @Override
    public HelpCenterEntity getById(Long helpDocId) {
        return helpDocDao.selectById(helpDocId);
    }

    @Override
    public com.budaos.support.domain.vo.HelpCenterDetailVO getDetail(Long helpDocId) {
        HelpCenterEntity entity = helpDocDao.selectById(helpDocId);
        com.budaos.support.domain.vo.HelpCenterDetailVO vo = new com.budaos.support.domain.vo.HelpCenterDetailVO();
        if (entity != null) {
            vo.setHelpDocId(entity.getHelpDocId());
            vo.setTitle(entity.getTitle());
            vo.setContentText(entity.getContentText());
            vo.setContentHtml(entity.getContentHtml());
            vo.setHelpDocCatalogId(entity.getHelpDocCatalogId());
            vo.setAuthor(entity.getAuthor());
            vo.setPageViewCount(entity.getPageViewCount());
            vo.setUserViewCount(entity.getUserViewCount());
            vo.setSort(entity.getSort());
            vo.setAttachment(entity.getAttachment());
            vo.setCreateTime(entity.getCreateTime());
            vo.setUpdateTime(entity.getUpdateTime());
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<String> add(HelpCenterEntity entity) {
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        helpDocDao.insert(entity);
        return ApiResult.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<String> update(HelpCenterEntity entity) {
        entity.setUpdateTime(LocalDateTime.now());
        helpDocDao.updateById(entity);
        return ApiResult.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<String> delete(Long helpDocId) {
        helpDocDao.deleteById(helpDocId);
        return ApiResult.ok();
    }

    @Override
    public void updateViewCount(Long helpDocId, Integer userViewCountIncrease, Integer pageViewCountIncrease) {
        helpDocDao.updateViewCount(helpDocId, userViewCountIncrease, pageViewCountIncrease);
    }

    @Override
    public List<HelpCenterVO> queryByCatalogId(Long helpDocCatalogId) {
        return helpDocDao.queryHelpDocByCatalogId(helpDocCatalogId);
    }

    @Override
    public List<HelpCenterVO> queryByRelationId(Long relationId) {
        return helpDocDao.queryHelpDocByRelationId(relationId);
    }
}
