package com.budaos.business.region.manager;

import com.budaos.business.region.dao.RegionDao;
import com.budaos.business.region.domain.vo.RegionTreeVO;
import com.budaos.business.region.domain.vo.RegionVO;
import com.budaos.common.core.constant.CacheKeyConstants;
import com.budaos.common.core.util.BeanCopyUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 区域缓存管理
 */
@Slf4j
@Service
public class RegionCacheManager {

    @Resource
    private RegionDao regionDao;

    @CacheEvict(value = {
            CacheKeyConstants.Region.REGION_LIST_CACHE,
            CacheKeyConstants.Region.REGION_TREE_CACHE
    }, allEntries = true)
    public void clearCache() {
        log.info("clear region cache");
    }

    /**
     * 区域列表
     */
    @Cacheable(CacheKeyConstants.Region.REGION_LIST_CACHE)
    public List<RegionVO> getRegionList() {
        return regionDao.listAll();
    }

    /**
     * 区域树结构
     */
    @Cacheable(CacheKeyConstants.Region.REGION_TREE_CACHE)
    public List<RegionTreeVO> getRegionTree() {
        List<RegionVO> regionVOList = regionDao.listAll();
        return buildTree(regionVOList);
    }

    /**
     * 构建区域树
     */
    private List<RegionTreeVO> buildTree(List<RegionVO> voList) {
        if (CollectionUtils.isEmpty(voList)) {
            return List.of();
        }

        // 获取所有根节点
        List<RegionVO> rootList = voList.stream()
                .filter(e -> e.getParentId() == null || e.getParentId().trim().isEmpty())
                .collect(Collectors.toList());

        List<RegionTreeVO> treeVOList = BeanCopyUtil.copyList(rootList, RegionTreeVO.class);
        buildTreeRecursive(treeVOList, voList);
        return treeVOList;
    }

    /**
     * 递归构建树
     */
    private void buildTreeRecursive(List<RegionTreeVO> parentList, List<RegionVO> allRegionList) {
        for (RegionTreeVO parent : parentList) {
            List<RegionVO> children = allRegionList.stream()
                    .filter(e -> parent.getId().equals(e.getParentId()))
                    .collect(Collectors.toList());

            if (CollectionUtils.isNotEmpty(children)) {
                List<RegionTreeVO> childTreeList = BeanCopyUtil.copyList(children, RegionTreeVO.class);
                parent.setChildren(childTreeList);
                buildTreeRecursive(childTreeList, allRegionList);
            }
        }
    }
}
