package com.budaos.support.controller;

import com.budaos.api.support.dto.AttachmentDownloadDTO;
import com.budaos.api.support.dto.AttachmentUploadDTO;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.common.web.annotation.AuditLog;
import com.budaos.support.domain.vo.AttachmentDownloadVO;
import com.budaos.support.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件控制器
 */
@RestController
@RequestMapping("/files")
@Tag(name = "文件访问", description = "文件访问接口")
@AuditLog(module = "文件管理", description = "文件操作")
public class AttachmentController {

    @Resource
    private FileStorageService fileStorageService;

    /**
     * 文件下载
     */
    @GetMapping("/download")
    @Operation(summary = "文件下载")
    public void download(@RequestParam("fileKey") String fileKey, jakarta.servlet.http.HttpServletResponse response) throws Exception {
        ApiResult<AttachmentDownloadVO> downloadResult = fileStorageService.download(fileKey);

        if (!downloadResult.getOk()) {
            response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("File not found");
            return;
        }

        AttachmentDownloadVO downloadVO = downloadResult.getData();
        byte[] fileData = downloadVO.getData();
        String fileName = downloadVO.getMetadata().getFileName();
        String contentType = "application/octet-stream";

        // 根据文件扩展名设置Content-Type
        if (fileName != null && fileName.contains(".")) {
            String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
            String detectedContentType = fileStorageService.getContentType(extension);
            if (detectedContentType != null && !detectedContentType.isEmpty()) {
                contentType = detectedContentType;
            }
        }

        response.setContentType(contentType);
        response.setContentLength(fileData.length);
        response.setHeader("Content-Disposition", "attachment; filename=\"" +
                java.net.URLEncoder.encode(fileName != null ? fileName : "download", java.nio.charset.StandardCharsets.UTF_8) + "\"");

        response.getOutputStream().write(fileData);
        response.getOutputStream().flush();
    }

    /**
     * 文件下载（保留原有接口以兼容）
     */
    @GetMapping("/{fileKey}")
    @Operation(summary = "文件下载（直接访问）")
    public void downloadByPath(@PathVariable String fileKey, jakarta.servlet.http.HttpServletResponse response) throws Exception {
        download(fileKey, response);
    }

    /**
     * 获取文件URL
     */
    @GetMapping("/url/{fileKey}")
    @Operation(summary = "获取文件URL")
    public ApiResult<String> getFileUrl(@PathVariable String fileKey) {
        return fileStorageService.getFileUrl(fileKey);
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/{fileKey}")
    @Operation(summary = "删除文件")
    public ApiResult<String> delete(@PathVariable("fileKey") String fileKey) {
        return fileStorageService.delete(fileKey);
    }
}

/**
 * 文件存储控制器 - 兼容接口（用于 Feign Client）
 * 保持 /support/file/getUrl 路径兼容性
 *
 * @author budaos
 */
@RestController
@RequestMapping("/support/file")
@Tag(name = "文件存储(兼容)", description = "文件存储Feign兼容接口")
class FileStorageController {

    @Resource
    private FileStorageService fileStorageService;

    /**
     * 文件上传
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "文件上传（兼容接口）")
    public ApiResult<AttachmentUploadDTO> upload(@RequestPart("file") MultipartFile file, @RequestParam("path") String path) {
        ApiResult<com.budaos.support.domain.vo.AttachmentUploadVO> result = fileStorageService.upload(file, path);
        if (!result.getOk()) {
            return ApiResult.error(result.getCode(), result.getMsg());
        }
        com.budaos.support.domain.vo.AttachmentUploadVO vo = result.getData();
        AttachmentUploadDTO dto = new AttachmentUploadDTO();
        dto.setFileId(vo.getFileId());
        dto.setFileName(vo.getFileName());
        dto.setFileUrl(vo.getFileUrl());
        dto.setFileKey(vo.getFileKey());
        dto.setFileSize(vo.getFileSize());
        dto.setFileType(vo.getFileType());
        return ApiResult.ok(dto);
    }

    /**
     * 获取文件url
     */
    @GetMapping("/getUrl")
    @Operation(summary = "获取文件URL（兼容接口）")
    public ApiResult<String> getFileUrl(@RequestParam("fileKey") String fileKey) {
        return fileStorageService.getFileUrl(fileKey);
    }

    /**
     * 流式下载（名称为原文件）
     */
    @GetMapping("/download")
    @Operation(summary = "文件下载（兼容接口）")
    public ApiResult<AttachmentDownloadDTO> download(@RequestParam("fileKey") String fileKey) {
        ApiResult<AttachmentDownloadVO> downloadResult = fileStorageService.download(fileKey);
        if (!downloadResult.getOk()) {
            return ApiResult.error(downloadResult.getCode(), downloadResult.getMsg());
        }
        AttachmentDownloadVO vo = downloadResult.getData();
        AttachmentDownloadDTO dto = new AttachmentDownloadDTO();
        dto.setData(vo.getData());
        AttachmentDownloadDTO.FileMetadataDTO metadata = new AttachmentDownloadDTO.FileMetadataDTO();
        metadata.setFileName(vo.getMetadata().getFileName());
        metadata.setContentType(fileStorageService.getContentType(vo.getMetadata().getFileFormat()));
        metadata.setFileSize(vo.getMetadata().getFileSize());
        metadata.setLastModified(null);
        dto.setMetadata(metadata);
        return ApiResult.ok(dto);
    }

    /**
     * 单个删除文件
     */
    @DeleteMapping("/{fileKey}")
    @Operation(summary = "删除文件（兼容接口）")
    public ApiResult<String> delete(@PathVariable("fileKey") String fileKey) {
        return fileStorageService.delete(fileKey);
    }
}
