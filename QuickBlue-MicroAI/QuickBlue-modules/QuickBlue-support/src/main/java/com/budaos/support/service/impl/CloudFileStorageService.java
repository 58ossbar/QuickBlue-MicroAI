package com.budaos.support.service.impl;

import com.budaos.common.core.code.SystemErrorCodes;
import com.budaos.common.core.util.StringUtil;
import com.budaos.support.constant.AttachmentFolderTypeEnum;
import com.budaos.support.dao.AttachmentDao;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.support.domain.vo.AttachmentDownloadVO;
import com.budaos.support.domain.vo.AttachmentMetadataVO;
import com.budaos.support.domain.vo.AttachmentUploadVO;
import com.budaos.support.domain.vo.AttachmentVO;
import com.budaos.support.service.FileStorageService;
import com.budaos.common.redis.cache.RedisCache;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 云存储实现
 *
 */
@Slf4j
public class CloudFileStorageService implements FileStorageService {

    /**
     * 自定义元数据 文件名称
     */
    private static final String USER_METADATA_FILE_NAME = "file-name";

    /**
     * 自定义元数据 文件格式
     */
    private static final String USER_METADATA_FILE_FORMAT = "file-format";

    /**
     * 自定义元数据 文件大小
     */
    private static final String USER_METADATA_FILE_SIZE = "file-size";

    @Resource
    private S3Client s3Client;

    @Value("${file.storage.cloud.bucket-name}")
    private String bucketName;

    @Value("${file.storage.cloud.region}")
    private String region;

    @Value("${file.storage.cloud.url-prefix}")
    private String urlPrefix;

    @Value("${file.storage.cloud.private-url-expire-seconds:3600}")
    private int privateUrlExpireSeconds;

    @Resource
    private AttachmentDao fileDao;

    @Resource
    private RedisCache redisCache;

    @Override
    public ApiResult<AttachmentUploadVO> upload(MultipartFile file, String path) {
        // 设置文件 key
        String originalFileName = file.getOriginalFilename();
        if (StringUtil.isEmpty(originalFileName)) {
            return ApiResult.userErrorParam("上传文件名为空");
        }

        String fileType = FilenameUtils.getExtension(originalFileName);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        String fileKey = path + uuid + "_" + time + "." + fileType;

        // 文件名称 URL 编码
        String urlEncoderFilename;
        urlEncoderFilename = URLEncoder.encode(originalFileName, StandardCharsets.UTF_8);
        Map<String, String> userMetadata = new HashMap<>(10);
        userMetadata.put(USER_METADATA_FILE_NAME, urlEncoderFilename);
        userMetadata.put(USER_METADATA_FILE_FORMAT, fileType);
        userMetadata.put(USER_METADATA_FILE_SIZE, String.valueOf(file.getSize()));

        // 根据文件路径获取并设置访问权限
        ObjectCannedACL acl = this.getACL(path);
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .metadata(userMetadata)
                .contentLength(file.getSize())
                .contentType(this.getContentType(fileType))
                .contentEncoding(StandardCharsets.UTF_8.name())
                .contentDisposition("attachment;filename=" + urlEncoderFilename)
                .acl(acl)
                .build();
        InputStream inputStream = null;
        try {
            inputStream = file.getInputStream();
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, file.getSize()));
        } catch (IOException e) {
            log.error("文件上传-发生异常：", e);
            return ApiResult.paramError(SystemErrorCodes.SYSTEM_ERROR.getMsg());
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        // 返回上传结果
        AttachmentUploadVO uploadVO = new AttachmentUploadVO();
        uploadVO.setFileName(originalFileName);
        uploadVO.setFileType(fileType);
        // 根据 访问权限 返回不同的 URL
        String url = urlPrefix + fileKey;
        if (ObjectCannedACL.PRIVATE.equals(acl)) {
            // 获取临时访问的URL
            url = this.getFileUrl(fileKey).getData();
        }
        uploadVO.setFileUrl(url);
        uploadVO.setFileKey(fileKey);
        uploadVO.setFileSize(file.getSize());
        return ApiResult.ok(uploadVO);
    }

    /**
     * 获取文件url
     *
     * @param fileKey 文件key
     * @return url
     */
    @Override
    public ApiResult<String> getFileUrl(String fileKey) {
        if (StringUtils.isBlank(fileKey)) {
            return ApiResult.userErrorParam("文件不存在，key为空");
        }

        if (!fileKey.startsWith(AttachmentFolderTypeEnum.FOLDER_PRIVATE.getFolder())) {
            // 不是私有的 都公共读
            return ApiResult.ok(urlPrefix + fileKey);
        }

        // 如果是私有的，则规定时间内可以访问，超过规定时间，则连接失效

        String fileRedisKey = "file:private:vo:" + fileKey;
        AttachmentVO fileVO = redisCache.get(fileRedisKey);

        if (fileVO == null) {
            fileVO = fileDao.getByFileKey(fileKey);
            if (fileVO == null) {
                return ApiResult.userErrorParam("文件不存在");
            }
            GetObjectRequest getUrlRequest = GetObjectRequest.builder().bucket(bucketName).key(fileKey).build();
            GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofSeconds(privateUrlExpireSeconds))
                    .getObjectRequest(getUrlRequest)
                    .build();

            S3Presigner presigner = S3Presigner.builder().region(Region.of(region)).build();

            PresignedGetObjectRequest presignedGetObjectRequest = presigner.presignGetObject(getObjectPresignRequest);
            String url = presignedGetObjectRequest.url().toString();
            fileVO.setFileUrl(url);
            // 缓存到Redis，过期时间比URL过期时间短5秒，避免返回即将过期的URL
            redisCache.set(fileRedisKey, fileVO, privateUrlExpireSeconds - 5);
        }

        return ApiResult.ok(fileVO.getFileUrl());
    }

    /**
     * 流式下载（名称为原文件）
     */
    @Override
    public ApiResult<AttachmentDownloadVO> download(String key) {

        // 获取文件 meta
        HeadObjectRequest objectRequest = HeadObjectRequest.builder().bucket(this.bucketName).key(key).build();
        HeadObjectResponse headObjectResponse = s3Client.headObject(objectRequest);
        Map<String, String> userMetadata = headObjectResponse.metadata();
        AttachmentMetadataVO metadataDTO = null;
        if (MapUtils.isNotEmpty(userMetadata)) {
            metadataDTO = new AttachmentMetadataVO();
            metadataDTO.setFileFormat(userMetadata.get(USER_METADATA_FILE_FORMAT));
            metadataDTO.setFileName(userMetadata.get(USER_METADATA_FILE_NAME));
            String fileSizeStr = userMetadata.get(USER_METADATA_FILE_SIZE);
            Long fileSize = StringUtils.isBlank(fileSizeStr) ? null : Long.valueOf(fileSizeStr);
            metadataDTO.setFileSize(fileSize);
        }

        //获取oss对象
        GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucketName).key(key).build();
        ResponseBytes<GetObjectResponse> s3ClientObject = s3Client.getObject(getObjectRequest, ResponseTransformer.toBytes());

        // 输入流转换为字节流
        byte[] buffer = s3ClientObject.asByteArray();
        AttachmentDownloadVO fileDownloadVO = new AttachmentDownloadVO();
        fileDownloadVO.setData(buffer);
        fileDownloadVO.setMetadata(metadataDTO);
        return ApiResult.ok(fileDownloadVO);
    }

    /**
     * 根据文件夹路径 返回对应的访问权限
     *
     * @param fileKey 文件key
     * @return 权限
     */
    private ObjectCannedACL getACL(String fileKey) {
        // 公用读
        if (fileKey.contains(AttachmentFolderTypeEnum.FOLDER_PUBLIC.getFolder())) {
            return ObjectCannedACL.PUBLIC_READ;
        }
        // 其他默认私有读写
        return ObjectCannedACL.PRIVATE;
    }

    /**
     * 单个删除文件
     * 根据 file key 删除文件
     * ps：不能删除fileKey不为空的文件夹
     *
     * @param fileKey 文件or文件夹
     */
    @Override
    public ApiResult<String> delete(String fileKey) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder().bucket(bucketName).key(fileKey).build();
        s3Client.deleteObject(deleteObjectRequest);
        return ApiResult.ok();
    }

}
