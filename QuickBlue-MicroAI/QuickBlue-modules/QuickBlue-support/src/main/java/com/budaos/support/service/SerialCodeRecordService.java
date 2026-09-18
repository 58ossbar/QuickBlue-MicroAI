package com.budaos.support.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.budaos.common.core.domain.PageResponse;
import com.budaos.support.dao.SerialCodeRecordDao;
import com.budaos.support.domain.entity.SerialCodeRecordEntity;
import com.budaos.support.domain.form.SerialCodeRecordQueryForm;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 单据序列号 记录
 *
 * @author budaos
 */
@Service
public class SerialCodeRecordService {

    @Resource
    private SerialCodeRecordDao serialNumberRecordDao;

    public PageResponse<SerialCodeRecordEntity> query(SerialCodeRecordQueryForm queryForm) {
        Page page = new Page<>(queryForm.getPageNum(), queryForm.getPageSize());
        List<SerialCodeRecordEntity> recordList = serialNumberRecordDao.query(page, queryForm);
        
        PageResponse<SerialCodeRecordEntity> result = new PageResponse<>();
        result.setPageNum((long) queryForm.getPageNum());
        result.setPageSize((long) queryForm.getPageSize());
        result.setTotal(page.getTotal());
        result.setList(recordList);
        
        return result;
    }
}
