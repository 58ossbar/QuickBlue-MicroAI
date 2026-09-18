package com.budaos.support.service.impl;

import com.budaos.common.core.domain.ApiResult;
import com.budaos.support.domain.vo.AttachmentDownloadVO;
import com.budaos.support.domain.vo.AttachmentMetadataVO;
import com.budaos.support.domain.vo.AttachmentUploadVO;
import com.budaos.support.service.FileStorageService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * 本地文件存储服务实现
 */
public class LocalFileStorageServiceImpl implements FileStorageService {

    @Value("${file.storage.local.upload-path:./uploads}")
    private String uploadPath;

    @Value("${file.storage.local.url-prefix:http://localhost:8083/files}")
    private String urlPrefix;

    @PostConstruct
    public void init() {
        // 确保上传目录存在
        try {
            Path path = Paths.get(uploadPath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ApiResult<AttachmentUploadVO> upload(MultipartFile file, String path) {
        try {
            // 获取原始文件名
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                return ApiResult.userErrorParam("文件名不能为空");
            }

            // 获取文件扩展名
            String fileExtension = getFileExtension(originalFilename);
            String contentType = getContentType(fileExtension);

            // 生成文件存储key
            String fileKey = generateFileKey(path, fileExtension);

            // 保存文件（fileKey 已包含完整路径）
            String fullFilePath = uploadPath + File.separator + fileKey.replace("/", File.separator);
            Path targetFilePath = Paths.get(fullFilePath);

            // 确保目标目录存在
            Path parentDir = targetFilePath.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }

            Files.copy(file.getInputStream(), targetFilePath, StandardCopyOption.REPLACE_EXISTING);

            // 构建返回结果
            AttachmentUploadVO uploadVO = new AttachmentUploadVO();
            uploadVO.setFileKey(fileKey);
            uploadVO.setFileUrl(urlPrefix + "/" + fileKey);
            uploadVO.setFileType(contentType);
            uploadVO.setFileSize(file.getSize());

            return ApiResult.ok(uploadVO);
        } catch (IOException e) {
            e.printStackTrace();
            return ApiResult.paramError("文件上传失败：" + e.getMessage());
        }
    }

    @Override
    public ApiResult<String> getFileUrl(String fileKey) {
        if (fileKey == null || fileKey.isEmpty()) {
            return ApiResult.userErrorParam("文件key不能为空");
        }

        String fileUrl = urlPrefix + "/" + fileKey;
        return ApiResult.ok(fileUrl);
    }

    @Override
    public ApiResult<AttachmentDownloadVO> download(String key) {
        try {
            String filePath = findFile(key);
            if (filePath == null) {
                return ApiResult.userErrorParam("文件不存在");
            }

            File file = new File(filePath);
            if (!file.exists()) {
                return ApiResult.userErrorParam("文件不存在");
            }

            byte[] fileContent = Files.readAllBytes(file.toPath());

            AttachmentDownloadVO downloadVO = new AttachmentDownloadVO();
            downloadVO.setData(fileContent);

            AttachmentMetadataVO metadata = new AttachmentMetadataVO();
            metadata.setFileName(file.getName());
            metadata.setFileSize(file.length());
            String contentType = Files.probeContentType(file.toPath());
            if (contentType != null && contentType.contains("/")) {
                metadata.setFileFormat(contentType.substring(contentType.lastIndexOf("/") + 1));
            }
            downloadVO.setMetadata(metadata);

            return ApiResult.ok(downloadVO);
        } catch (IOException e) {
            e.printStackTrace();
            return ApiResult.paramError("文件下载失败：" + e.getMessage());
        }
    }

    @Override
    public ApiResult<String> delete(String fileKey) {
        try {
            String filePath = findFile(fileKey);
            if (filePath == null) {
                return ApiResult.userErrorParam("文件不存在");
            }

            File file = new File(filePath);
            if (file.exists() && file.delete()) {
                return ApiResult.ok("文件删除成功");
            } else {
                return ApiResult.paramError("文件删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResult.paramError("文件删除失败：" + e.getMessage());
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1);
        }
        return "";
    }

    /**
     * 生成文件key
     */
    private String generateFileKey(String path, String fileExtension) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        if (path != null && !path.isEmpty()) {
            // 去掉 path 末尾的斜杠，避免双斜杠
            String normalizedPath = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
            return normalizedPath + "/" + uuid + "." + fileExtension;
        }
        return uuid + "." + fileExtension;
    }

    /**
     * 根据key查找文件
     */
    private String findFile(String fileKey) {
        if (fileKey.contains("/")) {
            // 如果key包含路径，直接拼接
            return uploadPath + File.separator + fileKey.replace("/", File.separator);
        } else {
            // 否则在上传目录下查找
            return uploadPath + File.separator + fileKey;
        }
    }
}
