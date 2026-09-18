package com.budaos.business.region.service;

import com.budaos.business.region.dao.RegionDao;
import com.budaos.business.region.domain.entity.RegionEntity;
import com.budaos.business.region.domain.form.RegionAddForm;
import com.budaos.business.region.domain.form.RegionQueryForm;
import com.budaos.business.region.domain.form.RegionUpdateForm;
import com.budaos.business.region.domain.vo.RegionTreeVO;
import com.budaos.business.region.domain.vo.RegionVO;
import com.budaos.business.region.manager.RegionCacheManager;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.common.core.util.BeanCopyUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 区域表（树形结构） Service
 */
@Service
public class RegionService {

    @Resource
    private RegionDao regionDao;

    @Resource
    private RegionCacheManager regionCacheManager;

    /**
     * 获取所有区域列表（树形结构）
     */
    public ApiResult<List<RegionTreeVO>> listAll() {
        List<RegionVO> regionList = regionCacheManager.getRegionList();
        List<RegionTreeVO> treeList = buildTree(regionList);
        return ApiResult.ok(treeList);
    }

    /**
     * 根据条件查询区域树
     */
    public ApiResult<List<RegionTreeVO>> queryTree(RegionQueryForm queryForm) {
        List<RegionVO> allRegions = regionCacheManager.getRegionList();

        // 如果没有查询条件，直接返回全部树
        if (StringUtils.isBlank(queryForm.getRegionName()) &&
                queryForm.getStatus() == null &&
                StringUtils.isBlank(queryForm.getRegionManagerName()) &&
                StringUtils.isBlank(queryForm.getContactPhone())) {
            List<RegionTreeVO> treeList = buildTree(allRegions);
            // 标记所有节点为匹配（因为没查询条件）
            markAllMatched(treeList, true);
            return ApiResult.ok(treeList);
        }

        // 筛选符合条件的区域
        Set<String> matchedIds = new HashSet<>();
        List<RegionVO> filteredRegions = allRegions.stream()
                .filter(region -> {
                    boolean isMatch = filterRegion(region, queryForm);
                    if (isMatch) {
                        matchedIds.add(region.getId());
                    }
                    return isMatch;
                })
                .collect(Collectors.toList());

        // 构建树时需要包含父节点，确保树结构完整
        List<RegionVO> completeRegions = ensureTreeComplete(filteredRegions, allRegions);
        List<RegionTreeVO> treeList = buildTree(completeRegions);

        // 标记匹配的节点
        markMatchedNodes(treeList, matchedIds);

        return ApiResult.ok(treeList);
    }
    /**
     * 标记匹配的节点
     */
    private void markMatchedNodes(List<RegionTreeVO> treeList, Set<String> matchedIds) {
        for (RegionTreeVO node : treeList) {
            // 设置匹配标记（需要在VO中添加这个字段）
            node.setMatched(matchedIds.contains(node.getId()));

            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                markMatchedNodes(node.getChildren(), matchedIds);
            }
        }
    }

    /**
     * 标记所有节点为匹配或不匹配
     */
    private void markAllMatched(List<RegionTreeVO> treeList, boolean matched) {
        for (RegionTreeVO node : treeList) {
            node.setMatched(matched);

            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                markAllMatched(node.getChildren(), matched);
            }
        }
    }
    /**
     * 确保树结构完整（包含父节点）
     */
    private List<RegionVO> ensureTreeComplete(List<RegionVO> filteredRegions, List<RegionVO> allRegions) {
        Set<String> neededIds = new HashSet<>();

        // 添加所有筛选出的节点ID
        filteredRegions.forEach(region -> neededIds.add(region.getId()));

        // 递归添加所有父节点
        for (RegionVO region : filteredRegions) {
            addParentIds(region.getId(), allRegions, neededIds);
        }

        // 返回包含所有需要节点的列表
        return allRegions.stream()
                .filter(region -> neededIds.contains(region.getId()))
                .collect(Collectors.toList());
    }

    /**
     * 递归添加父节点ID
     */
    private void addParentIds(String regionId, List<RegionVO> allRegions, Set<String> neededIds) {
        RegionVO region = allRegions.stream()
                .filter(r -> r.getId().equals(regionId))
                .findFirst()
                .orElse(null);

        if (region != null && StringUtils.isNotBlank(region.getParentId())) {
            neededIds.add(region.getParentId());
            addParentIds(region.getParentId(), allRegions, neededIds);
        }
    }

    /**
     * 筛选区域
     */
    private boolean filterRegion(RegionVO region, RegionQueryForm queryForm) {
        // 区域名称筛选
        boolean nameMatch = StringUtils.isBlank(queryForm.getRegionName()) ||
                (StringUtils.isNotBlank(region.getRegionName()) &&
                        region.getRegionName().toLowerCase().contains(queryForm.getRegionName().toLowerCase())) ||
                (StringUtils.isNotBlank(region.getRegionShortName()) &&
                        region.getRegionShortName().toLowerCase().contains(queryForm.getRegionName().toLowerCase()));

        // 状态筛选
        boolean statusMatch = queryForm.getStatus() == null ||
                (region.getStatus() != null && region.getStatus().equals(queryForm.getStatus()));

        // 负责人筛选
        boolean managerMatch = StringUtils.isBlank(queryForm.getRegionManagerName()) ||
                (StringUtils.isNotBlank(region.getRegionManagerName()) &&
                        region.getRegionManagerName().toLowerCase().contains(queryForm.getRegionManagerName().toLowerCase()));

        // 联系电话筛选
        boolean phoneMatch = StringUtils.isBlank(queryForm.getContactPhone()) ||
                (StringUtils.isNotBlank(region.getContactPhone()) &&
                        region.getContactPhone().contains(queryForm.getContactPhone()));

        return nameMatch && statusMatch && managerMatch && phoneMatch;
    }


    /**
     * 构建树形结构
     */
    private List<RegionTreeVO> buildTree(List<RegionVO> regionList) {
        if (CollectionUtils.isEmpty(regionList)) {
            return List.of();
        }

        // 获取所有根节点（parentId为null或空）
        List<RegionTreeVO> rootList = regionList.stream()
                .filter(item -> StringUtils.isBlank(item.getParentId()))
                .map(this::convertToTreeVO)
                .collect(Collectors.toList());

        // 递归构建树
        for (RegionTreeVO root : rootList) {
            buildChildren(root, regionList);
        }

        return rootList;
    }

    /**
     * 递归构建子节点
     */
    private void buildChildren(RegionTreeVO parent, List<RegionVO> allRegions) {
        List<RegionVO> children = allRegions.stream()
                .filter(item -> Objects.equals(item.getParentId(), parent.getId()))
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(children)) {
            List<RegionTreeVO> childTreeList = children.stream()
                    .map(this::convertToTreeVO)
                    .collect(Collectors.toList());

            parent.setChildren(childTreeList);

            // 递归构建孙子节点
            for (RegionTreeVO child : childTreeList) {
                buildChildren(child, allRegions);
            }
        }
    }

    /**
     * 转换为树形VO
     */
    private RegionTreeVO convertToTreeVO(RegionVO regionVO) {
        RegionTreeVO treeVO = BeanCopyUtil.copy(regionVO, RegionTreeVO.class);
        treeVO.setChildren(null); // 初始化为空
        return treeVO;
    }



    /**
     * 获取区域树形结构
     */
    public ApiResult<List<RegionTreeVO>> treeList() {
        List<RegionTreeVO> treeVOList = regionCacheManager.getRegionTree();
        return ApiResult.ok(treeVOList);
    }

    /**
     * 删除区域
     */
    @Transactional
    public ApiResult<String> delete(String id) {
        RegionEntity regionEntity = regionDao.selectById(id);
        if (regionEntity == null) {
            return ApiResult.userErrorParam("区域不存在");
        }

        // 检查是否有子区域
        int childCount = regionDao.countChildren(id);
        if (childCount > 0) {
            return ApiResult.userErrorParam("请先删除子级区域");
        }

        // 删除区域
        regionDao.deleteById(id);

        // 清除缓存
        regionCacheManager.clearCache();
        return ApiResult.ok();
    }
    /**
     * 添加区域
     */
    @Transactional
    public ApiResult<String> add(RegionAddForm addForm) {
        // 验证区域编码唯一性
        if (regionDao.existsByRegionCode(addForm.getRegionCode())) {
            return ApiResult.userErrorParam("区域编码已存在");
        }

        RegionEntity regionEntity = BeanCopyUtil.copy(addForm, RegionEntity.class);

        // 生成ID（如果前端没有传）
        if (StringUtils.isBlank(regionEntity.getId())) {
            regionEntity.setId(UUID.randomUUID().toString().replace("-", ""));
        }

        // 如果是根节点
        if (StringUtils.isBlank(addForm.getParentId())) {
            regionEntity.setParentId(null);
            regionEntity.setParentPath("/");
            regionEntity.setLevel(1);
        } else {
            // 获取父节点信息
            RegionEntity parentRegion = regionDao.selectById(addForm.getParentId());
            if (parentRegion == null) {
                return ApiResult.userErrorParam("父级区域不存在");
            }

            regionEntity.setParentPath(parentRegion.getParentPath() + parentRegion.getId() + "/");
            regionEntity.setLevel(parentRegion.getLevel() + 1);

            // 更新父节点为非叶子节点
            parentRegion.setLeafFlag(0);
            regionDao.updateById(parentRegion);
        }

        // 设置默认值
        regionEntity.setLeafFlag(1);
        regionEntity.setStatus(1);
        regionEntity.setDeleteFlag(0);

        // 创建时间和更新时间由 BaseEntity 自动处理
        // 如果 BaseEntity 不自动处理，可以手动设置
        // regionEntity.setCreateTime(LocalDateTime.now());
        // regionEntity.setUpdateTime(LocalDateTime.now());

        regionDao.insert(regionEntity);

        // 清除缓存
        regionCacheManager.clearCache();
        return ApiResult.ok();
    }
    /**
     * 更新区域
     */
    @Transactional
    public ApiResult<String> update(RegionUpdateForm updateForm) {
        RegionEntity regionEntity = regionDao.selectById(updateForm.getId());
        if (regionEntity == null) {
            return ApiResult.userErrorParam("区域不存在");
        }

        // 验证区域编码唯一性（排除自身）
        if (StringUtils.isNotBlank(updateForm.getRegionCode()) &&
                !updateForm.getRegionCode().equals(regionEntity.getRegionCode())) {
            if (regionDao.existsByRegionCodeAndIdNot(updateForm.getRegionCode(), updateForm.getId())) {
                return ApiResult.userErrorParam("区域编码已存在");
            }
        }

        // 更新区域信息
        regionEntity.setRegionCode(updateForm.getRegionCode());
        regionEntity.setRegionName(updateForm.getRegionName());
        regionEntity.setRegionShortName(updateForm.getRegionShortName());
        regionEntity.setSortOrder(updateForm.getSortOrder());
        regionEntity.setStatus(updateForm.getStatus());
        regionEntity.setRegionManagerName(updateForm.getRegionManagerName());
        regionEntity.setContactPhone(updateForm.getContactPhone());

        regionDao.updateById(regionEntity);

        // 清除缓存
        regionCacheManager.clearCache();
        return ApiResult.ok();
    }
}
