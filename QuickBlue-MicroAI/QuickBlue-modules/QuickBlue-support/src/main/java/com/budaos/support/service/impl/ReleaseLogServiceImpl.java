package com.budaos.support.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.common.core.util.PageConvertUtil;
import com.budaos.support.dao.ReleaseLogDao;
import com.budaos.support.domain.entity.ReleaseLogEntity;
import com.budaos.support.domain.form.ReleaseLogAddForm;
import com.budaos.support.domain.form.ReleaseLogQueryForm;
import com.budaos.support.domain.form.ReleaseLogUpdateForm;
import com.budaos.support.domain.vo.ReleaseLogVO;
import com.budaos.support.service.ReleaseLogService;
import com.budaos.common.core.util.BeanCopyUtil;
import com.budaos.common.core.util.PageConvertUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * 系统更新日志服务实现
 *
 * @author budaos
 */
@Service
public class ReleaseLogServiceImpl implements ReleaseLogService {

    @Resource
    private ReleaseLogDao changeLogDao;

    /**
     * 分页查询更新日志
     */
    @Override
    public PageResponse<ReleaseLogVO> query(ReleaseLogQueryForm queryForm) {
        Page<?> page = PageConvertUtil.convert2PageQuery(queryForm);
        java.util.List<ReleaseLogVO> dataList = changeLogDao.query(page, queryForm);
        return PageConvertUtil.convert2PageResult(page, dataList);
    }

    /**
     * 添加更新日志
     */
    @Override
    public ApiResult<String> add(ReleaseLogAddForm addForm) {
        // 检查版本是否已存在
        LambdaQueryWrapper<ReleaseLogEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReleaseLogEntity::getUpdateVersion, addForm.getUpdateVersion());
        ReleaseLogEntity exist = changeLogDao.selectOne(wrapper);
        if (exist != null) {
            return ApiResult.userErrorParam("版本号已存在");
        }

        ReleaseLogEntity changeLogEntity = BeanCopyUtil.copyProperties(addForm, ReleaseLogEntity.class);
        changeLogDao.insert(changeLogEntity);
        return ApiResult.ok();
    }

    /**
     * 更新更新日志
     */
    @Override
    public ApiResult<String> update(ReleaseLogUpdateForm updateForm) {
        ReleaseLogEntity changeLogEntity = changeLogDao.selectById(updateForm.getChangeLogId());
        if (changeLogEntity == null) {
            return ApiResult.userErrorParam("更新日志不存在");
        }

        // 检查版本号是否被其他记录使用
        LambdaQueryWrapper<ReleaseLogEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReleaseLogEntity::getUpdateVersion, updateForm.getUpdateVersion());
        wrapper.ne(ReleaseLogEntity::getChangeLogId, updateForm.getChangeLogId());
        ReleaseLogEntity exist = changeLogDao.selectOne(wrapper);
        if (exist != null) {
            return ApiResult.userErrorParam("版本号已存在");
        }

        changeLogDao.updateById(BeanCopyUtil.copyProperties(updateForm, ReleaseLogEntity.class));
        return ApiResult.ok();
    }

    /**
     * 删除更新日志
     */
    @Override
    public ApiResult<String> delete(Long changeLogId) {
        if (changeLogId == null) {
            return ApiResult.userErrorParam("更新日志ID不能为空");
        }
        changeLogDao.deleteById(changeLogId);
        return ApiResult.ok();
    }

    /**
     * 批量删除更新日志
     */
    @Override
    public ApiResult<String> batchDelete(java.util.List<Long> changeLogIdList) {
        if (changeLogIdList == null || changeLogIdList.isEmpty()) {
            return ApiResult.userErrorParam("更新日志ID列表不能为空");
        }
        changeLogDao.deleteBatchIds(changeLogIdList);
        return ApiResult.ok();
    }
}
