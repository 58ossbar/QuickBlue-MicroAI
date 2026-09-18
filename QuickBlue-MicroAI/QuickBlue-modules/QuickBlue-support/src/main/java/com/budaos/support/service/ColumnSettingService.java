package com.budaos.support.service;

import com.budaos.common.core.domain.CurrentUser;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.support.domain.ColumnSettingUpdateForm;

/**
 * 表格自定义列（前端用户自定义表格列，并保存到数据库里）
 */
public interface ColumnSettingService {

    /**
     * 获取 - 表格列
     */
    String getTableColumns(CurrentUser requestUser, Integer tableId);

    /**
     * 更新表格列
     */
    ApiResult<String> updateTableColumns(CurrentUser requestUser, ColumnSettingUpdateForm updateForm);

    /**
     * 删除表格列
     */
    ApiResult<String> deleteTableColumn(CurrentUser requestUser, Integer tableId);
}
