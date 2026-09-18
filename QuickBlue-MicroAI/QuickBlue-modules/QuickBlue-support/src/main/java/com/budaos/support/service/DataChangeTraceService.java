package com.budaos.support.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.budaos.support.domain.entity.DataChangeTraceEntity;
import com.budaos.support.domain.form.DataChangeTraceQueryForm;
import com.budaos.support.domain.vo.DataChangeTraceVO;

import java.util.List;

/**
 * 数据变动记录服务
 *
 * @author QuickBlue
 * @date 2026-02-08
 */
public interface DataChangeTraceService {

    /**
     * 分页查询
     *
     * @param page      分页参数
     * @param queryForm 查询表单
     * @return 分页结果
     */
    Page<DataChangeTraceVO> query(Page page, DataChangeTraceQueryForm queryForm);

    /**
     * 保存数据变动记录
     *
     * @param entity 变动记录实体
     * @return 是否成功
     */
    boolean saveTrace(DataChangeTraceEntity entity);

    /**
     * 批量保存数据变动记录
     *
     * @param entityList 变动记录实体列表
     * @return 是否成功
     */
    boolean saveTraceBatch(List<DataChangeTraceEntity> entityList);
}
