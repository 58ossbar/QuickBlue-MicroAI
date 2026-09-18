package com.budaos.system.manager;

import com.budaos.common.core.constant.CacheKeyConstants;
import com.budaos.common.core.util.BeanCopyUtil;
import com.budaos.system.dao.OrganizationDao;
import com.budaos.system.domain.entity.OrganizationEntity;
import com.budaos.system.domain.vo.OrganizationTreeVO;
import com.budaos.system.domain.vo.OrganizationVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 部门 缓存相关
 */
@Slf4j
@Service
public class OrganizationCacheManager {

    @Resource
    private OrganizationDao departmentDao;

    private void logClearInfo(String cache) {
        log.info("clear " + cache);
    }

    @CacheEvict(value = {
        CacheKeyConstants.Department.DEPARTMENT_LIST_CACHE,
        CacheKeyConstants.Department.DEPARTMENT_SELF_CHILDREN_CACHE,
        CacheKeyConstants.Department.DEPARTMENT_TREE_CACHE,
        CacheKeyConstants.Department.DEPARTMENT_PATH_CACHE
    }, allEntries = true)
    public void clearCache() {
        logClearInfo(CacheKeyConstants.Department.DEPARTMENT_LIST_CACHE);
    }

    /**
     * 部门列表
     */
    @Cacheable(CacheKeyConstants.Department.DEPARTMENT_LIST_CACHE)
    public List<OrganizationVO> getDepartmentList() {
        List<OrganizationEntity> entityList = departmentDao.selectList(null);
        return BeanCopyUtil.copyList(entityList, OrganizationVO.class);
    }

    /**
     * 缓存部门树结构
     */
    @Cacheable(CacheKeyConstants.Department.DEPARTMENT_TREE_CACHE)
    public List<OrganizationTreeVO> getDepartmentTree() {
        List<OrganizationVO> departmentVOList = getDepartmentList();
        return this.buildTree(departmentVOList);
    }

    /**
     * 缓存某个部门的下级id列表
     */
    @Cacheable(CacheKeyConstants.Department.DEPARTMENT_SELF_CHILDREN_CACHE)
    public List<Long> getDepartmentSelfAndChildren(Long departmentId) {
        List<OrganizationVO> departmentVOList = getDepartmentList();
        return this.selfAndChildrenIdList(departmentId, departmentVOList);
    }

    /**
     * 部门的路径名称
     */
    @Cacheable(CacheKeyConstants.Department.DEPARTMENT_PATH_CACHE)
    public Map<Long, String> getDepartmentPathMap() {
        List<OrganizationVO> departmentVOList = getDepartmentList();
        Map<Long, OrganizationVO> departmentMap = departmentVOList.stream()
                .collect(Collectors.toMap(OrganizationVO::getDepartmentId, Function.identity()));

        Map<Long, String> pathNameMap = departmentVOList.stream()
                .collect(Collectors.toMap(OrganizationVO::getDepartmentId, 
                    vo -> buildDepartmentPath(vo, departmentMap)));

        return pathNameMap;
    }

    /**
     * 构建父级考点路径
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

    // ---------------------- 构造树的一些方法 --------------

    /**
     * 构建部门树结构
     */
    public List<OrganizationTreeVO> buildTree(List<OrganizationVO> voList) {
        if (CollectionUtils.isEmpty(voList)) {
            return new ArrayList<>();
        }
        List<OrganizationVO> rootList = voList.stream()
                .filter(e -> e.getParentId() == null || Objects.equals(e.getParentId(), NumberUtils.LONG_ZERO))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(rootList)) {
            return new ArrayList<>();
        }
        List<OrganizationTreeVO> treeVOList = BeanCopyUtil.copyList(rootList, OrganizationTreeVO.class);
        this.recursiveBuildTree(treeVOList, voList);
        return treeVOList;
    }

    /**
     * 构建所有根节点的下级树形结构
     * 返回值为层序遍历结果
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
            return new ArrayList<>();
        }
        return BeanCopyUtil.copyList(childrenEntityList, OrganizationTreeVO.class);
    }

    /**
     * 通过部门id,获取当前以及下属部门
     */
    public List<Long> selfAndChildrenIdList(Long departmentId, List<OrganizationVO> voList) {
        List<Long> selfAndChildrenIdList = new ArrayList<>();
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
     * 递归查询
     */
    public void selfAndChildrenRecursion(List<Long> selfAndChildrenIdList, Long departmentId, List<OrganizationVO> voList) {
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
