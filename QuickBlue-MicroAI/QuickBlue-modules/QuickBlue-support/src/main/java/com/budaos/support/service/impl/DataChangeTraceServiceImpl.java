package com.budaos.support.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.budaos.support.dao.DataChangeTraceDao;
import com.budaos.support.domain.entity.DataChangeTraceEntity;
import com.budaos.support.domain.form.DataChangeTraceQueryForm;
import com.budaos.support.domain.vo.DataChangeTraceVO;
import com.budaos.support.service.DataChangeTraceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 数据变动记录服务实现
 *
 * @author QuickBlue
 * @date 2026-02-08
 */
@Slf4j
@Service
public class DataChangeTraceServiceImpl extends ServiceImpl<DataChangeTraceDao, DataChangeTraceEntity>
        implements DataChangeTraceService {

    @Override
    public Page<DataChangeTraceVO> query(Page page, DataChangeTraceQueryForm queryForm) {
        List<DataChangeTraceVO> list = baseMapper.query(page, queryForm);
        page.setRecords(list);
        return page;
    }

    @Override
    public boolean saveTrace(DataChangeTraceEntity entity) {
        return this.save(entity);
    }

    @Override
    public boolean saveTraceBatch(List<DataChangeTraceEntity> entityList) {
        return this.saveBatch(entityList);
    }
}
