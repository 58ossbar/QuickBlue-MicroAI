package com.budaos.support.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.budaos.support.dao.ColumnSettingDao;
import com.budaos.common.core.domain.CurrentUser;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.support.domain.entity.ColumnSettingEntity;
import com.budaos.support.service.ColumnSettingService;
import com.budaos.support.domain.ColumnSettingUpdateForm;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

/**
 * 表格自定义列（前端用户自定义表格列，并保存到数据库里）
 */
@Service
public class ColumnSettingServiceImpl implements ColumnSettingService {

    @Resource
    private ColumnSettingDao tableColumnDao;

    /**
     * 获取 - 表格列
     */
    @Override
    public String getTableColumns(CurrentUser requestUser, Integer tableId) {
        ColumnSettingEntity tableColumnEntity = tableColumnDao.selectByUserIdAndTableId(requestUser.getUserId(), requestUser.getUserType(), tableId);
        return tableColumnEntity == null ? null : tableColumnEntity.getColumns();
    }

    /**
     * 更新表格列
     */
    @Override
    public ApiResult<String> updateTableColumns(CurrentUser requestUser, ColumnSettingUpdateForm updateForm) {
        if (CollectionUtils.isEmpty(updateForm.getColumnList())) {
            return ApiResult.ok();
        }
        Integer tableId = updateForm.getTableId();
        ColumnSettingEntity tableColumnEntity = tableColumnDao.selectByUserIdAndTableId(requestUser.getUserId(), requestUser.getUserType(), tableId);
        if (tableColumnEntity == null) {
            tableColumnEntity = new ColumnSettingEntity();
            tableColumnEntity.setTableId(tableId);
            tableColumnEntity.setUserId(requestUser.getUserId());
            tableColumnEntity.setUserType(requestUser.getUserType());

            tableColumnEntity.setColumns(JSONArray.toJSONString(updateForm.getColumnList()));
            tableColumnDao.insert(tableColumnEntity);
        } else {
            tableColumnEntity.setColumns(JSONArray.toJSONString(updateForm.getColumnList()));
            tableColumnDao.updateById(tableColumnEntity);
        }
        return ApiResult.ok();
    }

    /**
     * 删除表格列
     */
    @Override
    public ApiResult<String> deleteTableColumn(CurrentUser requestUser, Integer tableId) {
        tableColumnDao.deleteTableColumn(requestUser.getUserId(), requestUser.getUserType(), tableId);
        return ApiResult.ok();
    }
}
