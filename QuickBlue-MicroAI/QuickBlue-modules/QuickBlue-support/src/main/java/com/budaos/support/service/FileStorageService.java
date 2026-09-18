package com.budaos.support.service;

import com.budaos.common.core.domain.ApiResult;
import com.budaos.support.domain.vo.AttachmentDownloadVO;
import com.budaos.support.domain.vo.AttachmentUploadVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储服务接口
 */
public interface FileStorageService {

    /**
     * 文件上传
     */
    ApiResult<AttachmentUploadVO> upload(MultipartFile file, String path);

    /**
     * 获取文件url
     */
    ApiResult<String> getFileUrl(String fileKey);

    /**
     * 流式下载（名称为原文件）
     */
    ApiResult<AttachmentDownloadVO> download(String key);

    /**
     * 单个删除文件
     */
    ApiResult<String> delete(String fileKey);

    /**
     * 获取文件类型
     */
    default String getContentType(String fileExt) {
        // 文件的后缀名
        if ("bmp".equalsIgnoreCase(fileExt)) {
            return "image/bmp";
        }
        if ("gif".equalsIgnoreCase(fileExt)) {
            return "image/gif";
        }
        if ("jpeg".equalsIgnoreCase(fileExt) || "jpg".equalsIgnoreCase(fileExt)) {
            return "image/jpeg";
        }
        if ("png".equalsIgnoreCase(fileExt)) {
            return "image/png";
        }
        if ("html".equalsIgnoreCase(fileExt)) {
            return "text/html";
        }
        if ("txt".equalsIgnoreCase(fileExt)) {
            return "text/plain";
        }
        if ("vsd".equalsIgnoreCase(fileExt)) {
            return "application/vnd.visio";
        }
        if ("ppt".equalsIgnoreCase(fileExt) || "pptx".equalsIgnoreCase(fileExt)) {
            return "application/vnd.ms-powerpoint";
        }
        if ("doc".equalsIgnoreCase(fileExt) || "docx".equalsIgnoreCase(fileExt)) {
            return "application/msword";
        }
        if ("pdf".equalsIgnoreCase(fileExt)) {
            return "application/pdf";
        }
        if ("xml".equalsIgnoreCase(fileExt)) {
            return "text/xml";
        }
        return "";
    }

}
