package com.budaos.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.system.dao.JobPostDao;
import com.budaos.system.domain.entity.JobPostEntity;
import com.budaos.system.domain.form.JobPostAddForm;
import com.budaos.system.domain.form.JobPostQueryForm;
import com.budaos.system.domain.form.JobPostUpdateForm;
import com.budaos.system.domain.vo.JobPostVO;
import com.budaos.common.core.util.BeanCopyUtil;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 职务表服务
 *
 * @author budaos
 */
@Service
public class JobPostService {

    @Resource
    private JobPostDao positionDao;

    /**
     * 分页查询
     *
     * @param queryForm 查询表单
     * @return 分页结果
     */
    public PageResponse<JobPostVO> queryPage(JobPostQueryForm queryForm) {
        queryForm.setDeletedFlag(Boolean.FALSE);
        Page<?> page = new Page<>(queryForm.getPageNum(), queryForm.getPageSize());
        List<JobPostVO> list = positionDao.queryPage(page, queryForm);
        if (list == null) {
            list = new java.util.ArrayList<>();
        }
        fillCategoryName(list);
        PageResponse<JobPostVO> pageResult = new PageResponse<>();
        pageResult.setList(list);
        pageResult.setTotal(page.getTotal());
        return pageResult;
    }

    /**
     * 添加职务
     *
     * @param addForm 添加表单
     * @return 响应
     */
    public ApiResult<String> add(JobPostAddForm addForm) {
        JobPostEntity positionEntity = BeanCopyUtil.copyProperties(addForm, JobPostEntity.class);
        if (positionEntity.getDeletedFlag() == null) {
            positionEntity.setDeletedFlag(Boolean.FALSE);
        }
        if (positionEntity.getStatus() == null) {
            positionEntity.setStatus(1);
        }
        positionDao.insert(positionEntity);
        return ApiResult.ok();
    }

    /**
     * 更新职务
     *
     * @param updateForm 更新表单
     * @return 响应
     */
    public ApiResult<String> update(JobPostUpdateForm updateForm) {
        JobPostEntity positionEntity = BeanCopyUtil.copyProperties(updateForm, JobPostEntity.class);
        positionDao.updateById(positionEntity);
        return ApiResult.ok();
    }

    /**
     * 批量删除职务
     *
     * @param idList 职务ID列表
     * @return 响应
     */
    public ApiResult<String> batchDelete(List<Long> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return ApiResult.ok();
        }
        positionDao.deleteBatchIds(idList);
        return ApiResult.ok();
    }

    /**
     * 删除职务
     *
     * @param positionId 职务ID
     * @return 响应
     */
    public ApiResult<String> delete(Long positionId) {
        if (null == positionId) {
            return ApiResult.ok();
        }
        positionDao.deleteById(positionId);
        return ApiResult.ok();
    }

    /**
     * 查询所有职务
     *
     * @return 职务VO列表
     */
    public List<JobPostVO> queryList() {
        List<JobPostVO> list = positionDao.queryList(Boolean.FALSE);
        fillCategoryName(list);
        return list;
    }

    /**
     * 查询岗位树（返回全量岗位，由前端组装树形结构）
     *
     * @return 职务VO列表
     */
    public List<JobPostVO> queryTree() {
        List<JobPostVO> list = positionDao.queryList(Boolean.FALSE);
        fillCategoryName(list);
        return list;
    }

    /**
     * 填充岗位类别名称（与编码保持一致，前端可配合字典组件做翻译）
     */
    private void fillCategoryName(List<JobPostVO> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (JobPostVO vo : list) {
            if (vo.getCategory() != null && vo.getCategoryName() == null) {
                vo.setCategoryName(vo.getCategory());
            }
        }
    }

}
