package com.budaos.support.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.budaos.common.core.domain.PageResponse;
import com.budaos.support.dao.KeepAliveRecordDao;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.support.domain.entity.KeepAliveRecordEntity;
import com.budaos.support.domain.form.KeepAliveRecordQueryForm;
import com.budaos.support.domain.vo.KeepAliveRecordVO;
import com.budaos.support.service.KeepAliveService;
import com.budaos.common.core.util.BeanCopyUtil;
import com.budaos.common.core.util.PageConvertUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 心跳记录服务实现
 *
 * @author budaos
 */
@Slf4j
@Service
public class KeepAliveServiceImpl implements KeepAliveService {

    @Resource
    private KeepAliveRecordDao heartBeatRecordDao;

    @Override
    public ApiResult<PageResponse<KeepAliveRecordVO>> pageQuery(KeepAliveRecordQueryForm pageParam) {
        Page pageQueryInfo = PageConvertUtil.convert2PageQuery(pageParam);
        List<KeepAliveRecordVO> recordVOList = heartBeatRecordDao.pageQuery(pageQueryInfo, pageParam);
        PageResponse<KeepAliveRecordVO> pageResult = PageConvertUtil.convert2PageResult(pageQueryInfo, recordVOList);
        return ApiResult.ok(pageResult);
    }
}
