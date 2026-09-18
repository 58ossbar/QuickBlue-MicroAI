package com.budaos.api.support.feign;

import com.budaos.api.support.dto.AttachmentDownloadDTO;
import com.budaos.api.support.dto.AttachmentUploadDTO;
import com.budaos.common.core.domain.ApiResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储服务 Feign Client
 *
 * @author budaos
 */
@FeignClient(contextId = "fileStorageFeignClient", value = "QuickBlue-support")
public interface FileStorageFeignClient {

    /**
     * 文件上传
     */
    @PostMapping(value = "/support/file/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResult<AttachmentUploadDTO> upload(@RequestPart("file") MultipartFile file, @RequestParam("path") String path);

    /**
     * 获取文件url
     */
    @GetMapping("/support/file/getUrl")
    ApiResult<String> getFileUrl(@RequestParam("fileKey") String fileKey);

    /**
     * 流式下载（名称为原文件）
     */
    @GetMapping("/support/file/download")
    ApiResult<AttachmentDownloadDTO> download(@RequestParam("fileKey") String fileKey);

    /**
     * 单个删除文件
     */
    @DeleteMapping("/support/file/{fileKey}")
    ApiResult<String> delete(@PathVariable("fileKey") String fileKey);
}
