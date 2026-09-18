package com.budaos.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.system.dao.OrganizationDao;
import com.budaos.system.dao.StaffDao;
import com.budaos.system.domain.entity.OrganizationEntity;
import com.budaos.system.domain.form.OrganizationAddForm;
import com.budaos.system.domain.form.OrganizationUpdateForm;
import com.budaos.system.domain.vo.OrganizationTreeVO;
import com.budaos.system.domain.vo.OrganizationVO;
import com.budaos.system.manager.OrganizationManager;
import com.budaos.system.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 部门（组织）服务实现
 *
 * @author budaos
 */
@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl extends ServiceImpl<OrganizationDao, OrganizationEntity> implements OrganizationService {

    /** 无父节点或父节点不可见时，统一提升为根节点 */
    private static final Long ROOT_PARENT_ID = 0L;

    private final OrganizationDao organizationDao;
    private final StaffDao staffDao;
    private final OrganizationManager organizationManager;

    @Override
    public ApiResult<List<OrganizationTreeVO>> departmentTree() {
        return ApiResult.ok(organizationManager.getDepartmentTree());
    }

    @Override
    public ApiResult<List<OrganizationTreeVO>> departmentTreeWithDataScope() {
        List<OrganizationVO> departmentVOList = listAllWithDataScope();
        if (CollectionUtils.isEmpty(departmentVOList)) {
            return ApiResult.ok(new ArrayList<>());
        }
        return ApiResult.ok(buildTree(departmentVOList));
    }

    /**
     * 将平铺的部门列表构建为带数据权限的树形结构
     *
     * @param voList 部门列表
     * @return 树形结构
     */
    private List<OrganizationTreeVO> buildTree(List<OrganizationVO> voList) {
        if (CollectionUtils.isEmpty(voList)) {
            return new ArrayList<>();
        }
        // 数据权限过滤后可能存在孤儿节点：父节点不在返回列表中，将其提升为根节点
        normalizeOrphanParentId(voList);

        List<OrganizationTreeVO> rootList = voList.stream()
                .filter(this::isRootNode)
                .map(this::copyToTreeVO)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(rootList)) {
            return new ArrayList<>();
        }
        recursiveBuildTree(rootList, voList);
        return rootList;
    }

    /**
     * 归一化孤儿节点：若父节点不在列表中，则将 parentId 置为根节点 ID，使其成为根节点
     */
    private void normalizeOrphanParentId(List<OrganizationVO> voList) {
        Set<Long> departmentIds = voList.stream()
                .map(OrganizationVO::getDepartmentId)
                .collect(Collectors.toSet());
        voList.stream()
                .filter(vo -> vo.getParentId() != null && !departmentIds.contains(vo.getParentId()))
                .forEach(vo -> vo.setParentId(ROOT_PARENT_ID));
    }

    private boolean isRootNode(OrganizationVO vo) {
        return vo.getParentId() == null || Objects.equals(vo.getParentId(), ROOT_PARENT_ID);
    }

    /**
     * 递归构建子树，并维护兄弟节点前后关系与"自身及所有子孙 ID"列表
     *
     * @param nodeList 当前层级节点列表
     * @param allDepartmentList 所有部门列表
     * @return 按层序遍历顺序排列的当前层及以下节点 ID 列表
     */
    private List<Long> recursiveBuildTree(List<OrganizationTreeVO> nodeList, List<OrganizationVO> allDepartmentList) {
        assignSiblingOrder(nodeList);

        List<Long> childIdList = new ArrayList<>();
        for (OrganizationTreeVO node : nodeList) {
            List<OrganizationTreeVO> children = getChildren(node.getDepartmentId(), allDepartmentList);

            List<Long> tempChildIdList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(children)) {
                node.setChildren(children);
                tempChildIdList = recursiveBuildTree(children, allDepartmentList);
            }

            appendSelfAndChildrenId(node, tempChildIdList);
            if (CollectionUtils.isNotEmpty(tempChildIdList)) {
                childIdList.addAll(tempChildIdList);
            }
        }

        // 将本层节点按顺序插入头部，保证层序遍历顺序
        for (int i = nodeList.size() - 1; i >= 0; i--) {
            childIdList.add(0, nodeList.get(i).getDepartmentId());
        }
        return childIdList;
    }

    /**
     * 设置同一层级内节点的前后兄弟关系（preId / nextId）
     */
    private void assignSiblingOrder(List<OrganizationTreeVO> nodeList) {
        for (int i = 0; i < nodeList.size(); i++) {
            if (i > 0) {
                nodeList.get(i).setPreId(nodeList.get(i - 1).getDepartmentId());
            }
            if (i < nodeList.size() - 1) {
                nodeList.get(i).setNextId(nodeList.get(i + 1).getDepartmentId());
            }
        }
    }

    /**
     * 将节点自身及所有子孙节点的 ID 追加到 selfAndAllChildrenIdList
     */
    private void appendSelfAndChildrenId(OrganizationTreeVO node, List<Long> childIdList) {
        List<Long> selfAndChildrenIds = node.getSelfAndAllChildrenIdList();
        if (selfAndChildrenIds == null || selfAndChildrenIds.isEmpty()) {
            selfAndChildrenIds = new ArrayList<>();
            node.setSelfAndAllChildrenIdList(selfAndChildrenIds);
        }
        selfAndChildrenIds.add(node.getDepartmentId());
        if (CollectionUtils.isNotEmpty(childIdList)) {
            selfAndChildrenIds.addAll(childIdList);
        }
    }

    /**
     * 获取子部门列表
     *
     * @param departmentId 部门ID
     * @param voList 所有部门列表
     * @return 子部门列表
     */
    private List<OrganizationTreeVO> getChildren(Long departmentId, List<OrganizationVO> voList) {
        List<OrganizationVO> childrenEntityList = voList.stream()
                .filter(vo -> departmentId.equals(vo.getParentId()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(childrenEntityList)) {
            return new ArrayList<>();
        }
        return childrenEntityList.stream()
                .map(this::copyToTreeVO)
                .collect(Collectors.toList());
    }

    /**
     * 将部门 VO 复制为树形 VO
     */
    private OrganizationTreeVO copyToTreeVO(OrganizationVO vo) {
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
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<String> addDepartment(OrganizationAddForm addForm) {
        // 验证部门名称
        if (StringUtils.isBlank(addForm.getDepartmentName())) {
            return ApiResult.userErrorParam("部门名称不能为空");
        }

        // 验证父部门是否存在
        if (addForm.getParentId() != null && organizationDao.selectById(addForm.getParentId()) == null) {
            return ApiResult.userErrorParam("父部门不存在");
        }

        // 验证部门负责人是否存在
        if (addForm.getManagerId() != null && staffDao.selectById(addForm.getManagerId()) == null) {
            return ApiResult.userErrorParam("部门负责人不存在");
        }

        OrganizationEntity departmentEntity = new OrganizationEntity();
        BeanUtils.copyProperties(addForm, departmentEntity);
        organizationDao.insert(departmentEntity);

        // 清除缓存
        organizationManager.clearCache();
        return ApiResult.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<String> updateDepartment(OrganizationUpdateForm updateForm) {
        if (updateForm.getDepartmentId() == null) {
            return ApiResult.userErrorParam("部门ID不能为空");
        }

        OrganizationEntity entity = organizationDao.selectById(updateForm.getDepartmentId());
        if (entity == null) {
            return ApiResult.userErrorParam("部门不存在");
        }

        // 验证父部门是否存在
        if (updateForm.getParentId() != null) {
            // 不能将部门设置为自己的子部门
            if (updateForm.getParentId().equals(updateForm.getDepartmentId())) {
                return ApiResult.userErrorParam("不能将部门设置为自己的父部门");
            }
            if (organizationDao.selectById(updateForm.getParentId()) == null) {
                return ApiResult.userErrorParam("父部门不存在");
            }
        }

        // 验证部门负责人是否存在
        if (updateForm.getManagerId() != null && staffDao.selectById(updateForm.getManagerId()) == null) {
            return ApiResult.userErrorParam("部门负责人不存在");
        }

        OrganizationEntity departmentEntity = new OrganizationEntity();
        BeanUtils.copyProperties(updateForm, departmentEntity);
        organizationDao.updateById(departmentEntity);

        // 清除缓存
        organizationManager.clearCache();
        return ApiResult.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<String> deleteDepartment(Long departmentId) {
        OrganizationEntity departmentEntity = organizationDao.selectById(departmentId);
        if (departmentEntity == null) {
            return ApiResult.userErrorParam("部门不存在");
        }

        // 是否有子级部门
        int subDepartmentNum = organizationDao.countSubDepartment(departmentId);
        if (subDepartmentNum > 0) {
            return ApiResult.userErrorParam("请先删除子级部门");
        }

        // 是否有未删除员工
        int employeeNum = staffDao.countByDepartmentId(departmentId, Boolean.FALSE);
        if (employeeNum > 0) {
            return ApiResult.userErrorParam("请先删除部门员工");
        }

        organizationDao.deleteById(departmentId);

        // 清除缓存
        organizationManager.clearCache();
        return ApiResult.ok();
    }

    @Override
    public List<OrganizationVO> listAll() {
        return organizationManager.getDepartmentList();
    }

    @Override
    public List<OrganizationVO> listAllWithDataScope() {
        List<OrganizationVO> departmentList = organizationDao.listAllWithDataScope();
        if (CollectionUtils.isEmpty(departmentList)) {
            return departmentList;
        }
        // 数据权限过滤后可能存在孤儿节点：父节点不在返回列表中，将其提升为根节点
        normalizeOrphanParentId(departmentList);
        return departmentList;
    }

    @Override
    public OrganizationVO getDepartmentById(Long departmentId) {
        return organizationDao.selectDepartmentVO(departmentId);
    }

    @Override
    public String getDepartmentPath(Long departmentId) {
        return organizationManager.getDepartmentPathMap().get(departmentId);
    }

    @Override
    public List<Long> selfAndChildrenIdList(Long departmentId) {
        return organizationManager.getDepartmentSelfAndChildren(departmentId);
    }
}
