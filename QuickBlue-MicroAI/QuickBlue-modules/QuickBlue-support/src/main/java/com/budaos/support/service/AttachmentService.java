package com.budaos.support.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.budaos.common.core.domain.PageResponse;
import com.budaos.support.constant.AttachmentFolderTypeEnum;
import com.budaos.common.core.domain.CurrentUser;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.support.domain.entity.AttachmentEntity;
import com.budaos.support.domain.form.AttachmentQueryForm;
import com.budaos.support.domain.vo.AttachmentDownloadVO;
import com.budaos.support.domain.vo.AttachmentUploadVO;
import com.budaos.support.domain.vo.AttachmentVO;
import com.budaos.common.core.util.EnumValueUtil;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 文件服务
 */
public interface AttachmentService {

    /**
     * 文件上传服务
     */
    ApiResult<AttachmentUploadVO> fileUpload(MultipartFile file, Integer folderType, CurrentUser requestUser);

    /**
     * 批量获取文件信息
     */
    List<AttachmentVO> getFileList(List<String> fileKeyList);

    /**
     * 根据文件绝对路径 获取文件URL
     */
    ApiResult<String> getFileUrl(String fileKeys);

    /**
     * 根据文件服务类型 和 FileKey 下载文件
     */
    ApiResult<AttachmentDownloadVO> getDownloadFile(String fileKey, String userAgent);

    /**
     * 分页查询
     */
    PageResponse<AttachmentVO> queryPage(AttachmentQueryForm queryForm);

    /**
     * 上传Base64格式的文件
     */
    String uploadBase64File(String base64Data, Integer folderType, String fileName, CurrentUser requestUser);

    /**
     * 高性能文件批量插入
     */
    int batchInsertFiles(List<AttachmentEntity> fileEntities);

    /**
     * 批量删除文件
     */
    int batchDeleteFiles(List<String> fileKeys);

    /**
     * 删除文件
     */
    boolean deleteFile(String fileKey);
}
