package com.budaos.support.service;

import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.support.domain.entity.ReleaseLogEntity;
import com.budaos.support.domain.form.ReleaseLogAddForm;
import com.budaos.support.domain.form.ReleaseLogQueryForm;
import com.budaos.support.domain.form.ReleaseLogUpdateForm;
import com.budaos.support.domain.vo.ReleaseLogVO;

/**
 * 系统更新日志服务接口
 *
 * @author budaos
 */
public interface ReleaseLogService {

    /**
     * 分页查询更新日志
     */
    PageResponse<ReleaseLogVO> query(ReleaseLogQueryForm queryForm);

    /**
     * 添加更新日志
     */
    ApiResult<String> add(ReleaseLogAddForm addForm);

    /**
     * 更新更新日志
     */
    ApiResult<String> update(ReleaseLogUpdateForm updateForm);

    /**
     * 删除更新日志
     */
    ApiResult<String> delete(Long changeLogId);

    /**
     * 批量删除更新日志
     */
    ApiResult<String> batchDelete(java.util.List<Long> changeLogIdList);
}
