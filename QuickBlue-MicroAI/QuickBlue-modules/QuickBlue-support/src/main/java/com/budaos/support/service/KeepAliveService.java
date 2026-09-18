package com.budaos.support.service;

import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.support.domain.form.KeepAliveRecordQueryForm;
import com.budaos.support.domain.vo.KeepAliveRecordVO;

/**
 * 心跳记录服务
 *
 * @author budaos
 */
public interface KeepAliveService {

    /**
     * 分页查询心跳记录
     *
     * @param pageParam 分页查询参数
     * @return 分页结果
     */
    ApiResult<PageResponse<KeepAliveRecordVO>> pageQuery(KeepAliveRecordQueryForm pageParam);
}
