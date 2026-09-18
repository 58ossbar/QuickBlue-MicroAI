package com.budaos.support.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.common.core.domain.CurrentUser;
import com.budaos.support.domain.entity.HelpCenterEntity;
import com.budaos.support.domain.form.HelpCenterViewRecordQueryForm;
import com.budaos.support.domain.vo.HelpCenterCatalogVO;
import com.budaos.support.domain.vo.HelpCenterCompleteVO;
import com.budaos.support.domain.vo.HelpCenterDetailVO;
import com.budaos.support.domain.vo.HelpCenterViewRecordVO;
import com.budaos.support.domain.vo.HelpCenterVO;
import com.budaos.support.service.HelpCenterCatalogService;
import com.budaos.support.service.HelpCenterUserService;
import com.budaos.common.core.util.BeanCopyUtil;
import com.budaos.common.core.util.PageConvertUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户查看帮助文档服务实现
 *
 * @author budaos
 */
@Service
public class HelpCenterUserServiceImpl implements HelpCenterUserService {

    @Resource
    private com.budaos.support.dao.HelpCenterDao helpDocDao;

    @Resource
    private HelpCenterCatalogService helpDocCatalogService;

    /**
     * 查询全部帮助文档
     */
    @Override
    public ApiResult<List<HelpCenterVO>> queryAllHelpDocList() {
        return ApiResult.ok(helpDocDao.queryAllHelpDocList());
    }

    /**
     * 查询我的待查看的帮助文档清单
     */
    @Override
    public ApiResult<HelpCenterDetailVO> view(CurrentUser requestUser, Long helpDocId) {
        HelpCenterEntity helpDocEntity = helpDocDao.selectById(helpDocId);
        if (helpDocEntity == null) {
            return ApiResult.userErrorParam("帮助文档不存在");
        }

        HelpCenterDetailVO helpDocDetailVO = BeanCopyUtil.copyProperties(helpDocEntity, HelpCenterDetailVO.class);
        long viewCount = helpDocDao.viewRecordCount(helpDocId, requestUser.getUserId());
        if (viewCount == 0) {
            helpDocDao.insertViewRecord(helpDocId, requestUser.getUserId(), requestUser.getUserName(),
                    requestUser.getIp(), requestUser.getUserAgent(), 1);
            helpDocDao.updateViewCount(helpDocId, 1, 1);
            helpDocDetailVO.setPageViewCount(helpDocDetailVO.getPageViewCount() + 1);
            helpDocDetailVO.setUserViewCount(helpDocDetailVO.getUserViewCount() + 1);
        } else {
            helpDocDao.updateViewRecord(helpDocId, requestUser.getUserId(), requestUser.getIp(), requestUser.getUserAgent());
            helpDocDao.updateViewCount(helpDocId, 0, 1);
            helpDocDetailVO.setPageViewCount(helpDocDetailVO.getPageViewCount() + 1);
        }

        return ApiResult.ok(helpDocDetailVO);
    }

    /**
     * 分页查询查看记录
     */
    @Override
    public PageResponse<HelpCenterViewRecordVO> queryViewRecord(HelpCenterViewRecordQueryForm helpDocViewRecordQueryForm) {
        Page<?> page = PageConvertUtil.convert2PageQuery(helpDocViewRecordQueryForm);
        List<HelpCenterViewRecordVO> noticeViewRecordVOS = helpDocDao.queryViewRecordList(page, helpDocViewRecordQueryForm);
        return PageConvertUtil.convert2PageResult(page, noticeViewRecordVOS);
    }

    /**
     * 获取完整的帮助文档数据（包含所有目录和文档内容，用于导出PDF）
     */
    @Override
    public HelpCenterCompleteVO getCompleteHelpDoc() {
        HelpCenterCompleteVO completeVO = new HelpCenterCompleteVO();
        completeVO.setTitle("帮助文档");

        // 获取所有目录
        List<HelpCenterCatalogVO> catalogList = helpDocCatalogService.getAll();

        // 获取所有文档详情（包含contentHtml）
        List<HelpCenterDetailVO> allDocDetailList = helpDocDao.queryAllHelpDocDetailList();

        // 转换为Map，方便查找
        Map<Long, List<HelpCenterDetailVO>> catalogDocMap = new HashMap<>();
        for (HelpCenterDetailVO doc : allDocDetailList) {
            Long catalogId = doc.getHelpDocCatalogId();
            if (catalogId == null) {
                catalogId = 0L;
            }
            List<HelpCenterDetailVO> docList = catalogDocMap.computeIfAbsent(catalogId, k -> new ArrayList<>());
            docList.add(doc);
        }

        // 按sort排序
        for (List<HelpCenterDetailVO> docList : catalogDocMap.values()) {
            docList.sort(Comparator.comparing(HelpCenterDetailVO::getSort));
        }

        // 构建目录树
        completeVO.setCatalogTree(buildCatalogTree(catalogList, 0L, catalogDocMap));

        return completeVO;
    }

    /**
     * 构建目录树
     */
    private List<HelpCenterCompleteVO.HelpDocCatalogTreeVO> buildCatalogTree(
            List<HelpCenterCatalogVO> catalogList,
            Long parentId,
            Map<Long, List<HelpCenterDetailVO>> catalogDocMap) {

        List<HelpCenterCatalogVO> childCatalogs = new ArrayList<>();
        for (HelpCenterCatalogVO catalog : catalogList) {
            if (parentId.equals(catalog.getParentId())) {
                childCatalogs.add(catalog);
            }
        }

        // 按sort排序
        childCatalogs.sort(Comparator.comparing(HelpCenterCatalogVO::getSort));

        List<HelpCenterCompleteVO.HelpDocCatalogTreeVO> treeNodes = new ArrayList<>();

        for (HelpCenterCatalogVO catalog : childCatalogs) {
            HelpCenterCompleteVO.HelpDocCatalogTreeVO treeNode = new HelpCenterCompleteVO.HelpDocCatalogTreeVO();
            treeNode.setHelpDocCatalogId(catalog.getHelpDocCatalogId());
            treeNode.setName(catalog.getName());
            treeNode.setSort(catalog.getSort());
            treeNode.setParentId(catalog.getParentId());

            // 获取该目录下的文档（直接从Map中获取，避免重复查询）
            List<HelpCenterDetailVO> docList = catalogDocMap.get(catalog.getHelpDocCatalogId());
            if (docList != null) {
                treeNode.setDocList(docList);
            }

            // 递归构建子目录
            List<HelpCenterCompleteVO.HelpDocCatalogTreeVO> children = buildCatalogTree(
                    catalogList,
                    catalog.getHelpDocCatalogId(),
                    catalogDocMap);
            treeNode.setChildren(children);

            treeNodes.add(treeNode);
        }

        return treeNodes;
    }
}
