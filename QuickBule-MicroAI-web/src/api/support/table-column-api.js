/*
 * @Description:表格自定义列

 */
import { postRequest, getRequest } from '/@/lib/axios';

export const tableColumnApi = {
    // 修改表格列 
    updateTableColumn: (param) => {
        return postRequest('/support/tableColumn/update', param);
    },

    // 查询表格列 
    getColumns: (tableId) => {
        return getRequest(`/support/tableColumn/getColumns/${tableId}`);
    },

    // 删除表格列 
    deleteColumns: (tableId) => {
        return getRequest(`/support/tableColumn/delete/${tableId}`);
    },
};
