package com.budaos.system.manager;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.budaos.system.dao.OrganizationDao;
import com.budaos.system.domain.vo.OrganizationTreeVO;
import com.budaos.system.domain.vo.OrganizationVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 部门缓存管理器
 *
 * @author budaos
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrganizationManager {

    private final OrganizationDao departmentDao;

    /**
     * 清除缓存
     */
    @CacheEvict(value = {"department:list", "department:tree", "department:selfChildren", "department:path"}, allEntries = true)
    public void clearCache() {
        log.info("清除部门缓存");
    }

    /**
     * 获取部门列表
     */
    @Cacheable(value = "department:list", unless = "#result == null || #result.isEmpty()")
    public List<OrganizationVO> getDepartmentList() {
        return departmentDao.listAll();
    }

    /**
     * 获取部门树结构
     */
    @Cacheable(value = "department:tree", unless = "#result == null || #result.isEmpty()")
    public List<OrganizationTreeVO> getDepartmentTree() {
        List<OrganizationVO> departmentVOList = departmentDao.listAll();
        return buildTree(departmentVOList);
    }

    /**
     * 获取某个部门的下级ID列表
     */
    @Cacheable(value = "department:selfChildren", key = "#departmentId", unless = "#result == null || #result.isEmpty()")
    public List<Long> getDepartmentSelfAndChildren(Long departmentId) {
        List<OrganizationVO> departmentVOList = departmentDao.listAll();
        return selfAndChildrenIdList(departmentId, departmentVOList);
    }

    /**
     * 获取部门路径名称映射
     */
    @Cacheable(value = "department:path", unless = "#result == null || #result.isEmpty()")
    public Map<Long, String> getDepartmentPathMap() {
        List<OrganizationVO> departmentVOList = departmentDao.listAll();
        Map<Long, OrganizationVO> departmentMap = departmentVOList.stream()
                .collect(Collectors.toMap(OrganizationVO::getDepartmentId, Function.identity()));

        Map<Long, String> pathNameMap = Maps.newHashMap();
        for (OrganizationVO departmentVO : departmentVOList) {
            String pathName = buildDepartmentPath(departmentVO, departmentMap);
            pathNameMap.put(departmentVO.getDepartmentId(), pathName);
        }

        return pathNameMap;
    }

    /**
     * 构建部门路径
     */
    private String buildDepartmentPath(OrganizationVO departmentVO, Map<Long, OrganizationVO> departmentMap) {
        if (Objects.equals(departmentVO.getParentId(), NumberUtils.LONG_ZERO)) {
            return departmentVO.getDepartmentName();
        }
        // 父节点
        OrganizationVO parentDepartment = departmentMap.get(departmentVO.getParentId());
        if (parentDepartment == null) {
            return departmentVO.getDepartmentName();
        }
        String pathName = buildDepartmentPath(parentDepartment, departmentMap);
        return pathName + "/" + departmentVO.getDepartmentName();
    }

    /**
     * 构建部门树结构
     */
    private List<OrganizationTreeVO> buildTree(List<OrganizationVO> voList) {
        if (CollectionUtils.isEmpty(voList)) {
            return Lists.newArrayList();
        }
        List<OrganizationVO> rootList = voList.stream()
                .filter(e -> e.getParentId() == null || Objects.equals(e.getParentId(), NumberUtils.LONG_ZERO))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(rootList)) {
            return Lists.newArrayList();
        }
        List<OrganizationTreeVO> treeVOList = copyToTreeVOList(rootList);
        recursiveBuildTree(treeVOList, voList);
        return treeVOList;
    }

    /**
     * 递归构建树结构
     */
    private List<Long> recursiveBuildTree(List<OrganizationTreeVO> nodeList, List<OrganizationVO> allDepartmentList) {
        int nodeSize = nodeList.size();
        List<Long> childIdList = new ArrayList<>();
        for (int i = 0; i < nodeSize; i++) {
            int preIndex = i - 1;
            int nextIndex = i + 1;
            OrganizationTreeVO node = nodeList.get(i);
            if (preIndex > -1) {
                node.setPreId(nodeList.get(preIndex).getDepartmentId());
            }
            if (nextIndex < nodeSize) {
                node.setNextId(nodeList.get(nextIndex).getDepartmentId());
            }

            List<OrganizationTreeVO> children = getChildren(node.getDepartmentId(), allDepartmentList);

            List<Long> tempChildIdList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(children)) {
                node.setChildren(children);
                tempChildIdList = this.recursiveBuildTree(children, allDepartmentList);
            }

            if (CollectionUtils.isEmpty(node.getSelfAndAllChildrenIdList())) {
                node.setSelfAndAllChildrenIdList(new ArrayList<>());
            }
            node.getSelfAndAllChildrenIdList().add(node.getDepartmentId());

            if (CollectionUtils.isNotEmpty(tempChildIdList)) {
                node.getSelfAndAllChildrenIdList().addAll(tempChildIdList);
                childIdList.addAll(tempChildIdList);
            }
        }

        // 保证本层遍历顺序
        for (int i = nodeSize - 1; i >= 0; i--) {
            childIdList.add(0, nodeList.get(i).getDepartmentId());
        }

        return childIdList;
    }

    /**
     * 获取子元素
     */
    private List<OrganizationTreeVO> getChildren(Long departmentId, List<OrganizationVO> voList) {
        List<OrganizationVO> childrenEntityList = voList.stream()
                .filter(e -> departmentId.equals(e.getParentId()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(childrenEntityList)) {
            return Lists.newArrayList();
        }
        return copyToTreeVOList(childrenEntityList);
    }

    /**
     * 复制VO到TreeVO
     */
    private List<OrganizationTreeVO> copyToTreeVOList(List<OrganizationVO> voList) {
        if (CollectionUtils.isEmpty(voList)) {
            return Lists.newArrayList();
        }
        return voList.stream().map(vo -> {
            OrganizationTreeVO treeVO = new OrganizationTreeVO();
            treeVO.setDepartmentId(vo.getDepartmentId());
            treeVO.setDepartmentName(vo.getDepartmentName());
            treeVO.setManagerId(vo.getManagerId());
            treeVO.setManagerName(vo.getManagerName());
            treeVO.setParentId(vo.getParentId());
            treeVO.setSort(vo.getSort());
            treeVO.setCreateTime(vo.getCreateTime());
            treeVO.setUpdateTime(vo.getUpdateTime());
            return treeVO;
        }).collect(Collectors.toList());
    }

    /**
     * 获取自己及所有子部门的ID列表
     */
    private List<Long> selfAndChildrenIdList(Long departmentId, List<OrganizationVO> voList) {
        List<Long> selfAndChildrenIdList = Lists.newArrayList();
        if (CollectionUtils.isEmpty(voList)) {
            return selfAndChildrenIdList;
        }
        selfAndChildrenIdList.add(departmentId);
        List<OrganizationTreeVO> children = this.getChildren(departmentId, voList);
        if (CollectionUtils.isEmpty(children)) {
            return selfAndChildrenIdList;
        }
        List<Long> childrenIdList = children.stream()
                .map(OrganizationTreeVO::getDepartmentId)
                .collect(Collectors.toList());
        selfAndChildrenIdList.addAll(childrenIdList);
        for (Long childId : childrenIdList) {
            this.selfAndChildrenRecursion(selfAndChildrenIdList, childId, voList);
        }
        return selfAndChildrenIdList;
    }

    /**
     * 递归获取子部门ID
     */
    private void selfAndChildrenRecursion(List<Long> selfAndChildrenIdList, Long departmentId, List<OrganizationVO> voList) {
        List<OrganizationTreeVO> children = this.getChildren(departmentId, voList);
        if (CollectionUtils.isEmpty(children)) {
            return;
        }
        List<Long> childrenIdList = children.stream()
                .map(OrganizationTreeVO::getDepartmentId)
                .collect(Collectors.toList());
        selfAndChildrenIdList.addAll(childrenIdList);
        for (Long childId : childrenIdList) {
            this.selfAndChildrenRecursion(selfAndChildrenIdList, childId, voList);
        }
    }
}
