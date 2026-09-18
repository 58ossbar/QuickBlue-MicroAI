/*
 * 文件上传
 *
 
 */
import { request, getRequest, getDownload } from '/@/lib/axios';

export const fileApi = {
    // 文件上传
    uploadUrl: '/support/file/upload',
    uploadFile: (param, folderType) => {
        // 构建FormData对象
        const formData = param instanceof FormData ? param : new FormData();
        // 添加folderType参数到FormData
        if (folderType !== undefined) {
            formData.append('folderType', folderType);
        }
        return request({
            url: '/support/file/upload',
            method: 'post',
            data: formData,
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        });
    },

    /**
     * 分页查询
     */
    queryPage: (param) => {
        return postRequest('/support/file/queryPage', param);
    },
    /**
     * 获取文件URL：根据fileKey
     */
    getUrl: (fileKey) => {
        return getRequest(`/support/files/url/${fileKey}`);
    },

    /**
     * 下载文件流（根据fileKey）
     */
    downLoadFile: (fileKey) => {
        return getDownload(`/support/files/download?fileKey=${encodeURIComponent(fileKey)}`, {});
    },
};