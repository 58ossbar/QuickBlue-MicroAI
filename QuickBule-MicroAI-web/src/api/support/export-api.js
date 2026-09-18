import { request } from '/@/lib/axios';

export const exportApi = {
    /**
     * 通用 Excel 导出（服务端生成，带完整样式）
     * @param {Object} params - { headers, rows, fileName, sheetName }
     */
    exportExcel: (params) => {
        return request({
            url: '/support/export/excel',
            method: 'post',
            data: params,
            responseType: 'blob',
        });
    },
};
