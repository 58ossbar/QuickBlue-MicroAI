package com.budaos.support.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.CurrentUser;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.support.constant.AttachmentFolderTypeEnum;
import com.budaos.support.dao.AttachmentDao;
import com.budaos.support.domain.entity.AttachmentEntity;
import com.budaos.support.domain.form.AttachmentQueryForm;
import com.budaos.support.domain.vo.AttachmentDownloadVO;
import com.budaos.support.domain.vo.AttachmentUploadVO;
import com.budaos.support.domain.vo.AttachmentVO;
import com.budaos.common.core.exception.BizException;
import com.budaos.support.securityprotect.service.SecureFileService;
import com.budaos.support.service.AttachmentService;
import com.budaos.support.service.FileStorageService;
import com.budaos.common.core.util.PageConvertUtil;
import com.budaos.common.core.util.EnumValueUtil;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 文件服务实现
 */
@Service
@Slf4j
public class AttachmentServiceImpl implements AttachmentService {

    /**
     * 文件名最大长度
     */
    private static final int FILE_NAME_MAX_LENGTH = 100;

    /**
     * 批量插入批次大小
     */
    private static final int BATCH_INSERT_SIZE = 1000;

    /**
     * JDBC批量大小
     */
    private static final int JDBC_BATCH_SIZE = 500;

    /**
     * 降级批次大小
     */
    private static final int FALLBACK_BATCH_SIZE = 100;

    /**
     * 日志记录间隔
     */
    private static final int LOG_INTERVAL = 1000;

    @Resource
    private FileStorageService fileStorageService;

    @Resource
    private AttachmentDao fileDao;

    @Resource
    private SecureFileService securityFileService;

    @Resource
    private JdbcTemplate jdbcTemplate;

    /**
     * 文件上传服务
     */
    @Override
    public ApiResult<AttachmentUploadVO> fileUpload(MultipartFile file, Integer folderType, CurrentUser requestUser) {
        AttachmentFolderTypeEnum folderTypeEnum = EnumValueUtil.getEnumByValue(folderType, AttachmentFolderTypeEnum.class);
        if (null == folderTypeEnum) {
            return ApiResult.userErrorParam("文件夹错误");
        }

        if (null == file || file.getSize() == 0) {
            return ApiResult.userErrorParam("上传文件不能为空");
        }

        // 校验文件名称
        String originalFilename = file.getOriginalFilename();
        if (StringUtils.isBlank(originalFilename)) {
            return ApiResult.userErrorParam("上传文件名称不能为空");
        }

        if (originalFilename.length() > FILE_NAME_MAX_LENGTH) {
            return ApiResult.userErrorParam("文件名称最大长度为：" + FILE_NAME_MAX_LENGTH);
        }

        // 校验文件大小以及安全性
        ApiResult<String> validateFile = securityFileService.checkFile(file);
        if (!validateFile.getOk()) {
            return ApiResult.paramError(validateFile.getMsg());
        }

        // 进行上传
        ApiResult<AttachmentUploadVO> response = fileStorageService.upload(file, folderTypeEnum.getFolder());
        if (!response.getOk()) {
            return response;
        }

        // 上传成功 保存记录数据库
        AttachmentUploadVO uploadVO = response.getData();
        AttachmentEntity fileEntity = new AttachmentEntity();
        fileEntity.setFolderType(folderTypeEnum.getValue());
        fileEntity.setFileName(originalFilename);
        fileEntity.setFileSize(file.getSize());
        fileEntity.setFileKey(uploadVO.getFileKey());
        fileEntity.setFileType(uploadVO.getFileType());
        fileEntity.setCreatorId(requestUser == null ? null : requestUser.getUserId());
        fileEntity.setCreatorName(requestUser == null ? null : requestUser.getUserName());
        fileEntity.setCreatorUserType(requestUser == null ? null : requestUser.getUserType());
        fileDao.insert(fileEntity);

        // 将fileId 返回给前端
        uploadVO.setFileId(fileEntity.getFileId());

        return response;
    }

    /**
     * 批量获取文件信息
     */
    @Override
    public List<AttachmentVO> getFileList(List<String> fileKeyList) {
        if (CollectionUtils.isEmpty(fileKeyList)) {
            return new ArrayList<>();
        }

        // 查询数据库，并获取 file url
        Set<String> fileKeySet = new HashSet<>(fileKeyList);
        Map<String, AttachmentVO> fileMap = fileDao.selectByFileKeyList(fileKeySet)
                .stream().collect(Collectors.toMap(AttachmentVO::getFileKey, Function.identity()));

        for (AttachmentVO fileVO : fileMap.values()) {
            ApiResult<String> fileUrlResponse = fileStorageService.getFileUrl(fileVO.getFileKey());
            if (fileUrlResponse.getOk()) {
                fileVO.setFileUrl(fileUrlResponse.getData());
            }
        }

        // 返回结果
        List<AttachmentVO> result = new ArrayList<>(fileKeyList.size());
        for (String fileKey : fileKeyList) {
            AttachmentVO fileVO = fileMap.get(fileKey);
            if (fileVO != null) {
                result.add(fileVO);
            }
        }

        return result;
    }

    /**
     * 根据文件绝对路径 获取文件URL
     */
    @Override
    public ApiResult<String> getFileUrl(String fileKeys) {
        if (StringUtils.isBlank(fileKeys)) {
            return ApiResult.paramError("参数错误");
        }

        String[] fileKeyArray = fileKeys.split(",");
        List<String> fileUrlList = new ArrayList<>(fileKeyArray.length);
        for (String fileKey : fileKeyArray) {
            ApiResult<String> fileUrlResponse = fileStorageService.getFileUrl(fileKey);
            if (fileUrlResponse.getOk()) {
                fileUrlList.add(fileUrlResponse.getData());
            }
        }
        return ApiResult.ok(String.join(",", fileUrlList));
    }

    /**
     * 根据文件服务类型 和 FileKey 下载文件
     */
    @Override
    public ApiResult<AttachmentDownloadVO> getDownloadFile(String fileKey, String userAgent) {
        AttachmentVO fileVO = fileDao.getByFileKey(fileKey);
        if (fileVO == null) {
            return ApiResult.userErrorParam("文件不存在");
        }

        // 根据文件服务类 获取对应文件服务 查询 url
        ApiResult<AttachmentDownloadVO> download = fileStorageService.download(fileKey);
        if (download.getOk()) {
            download.getData().getMetadata().setFileName(fileVO.getFileName());
        }
        return download;
    }

    /**
     * 分页查询
     */
    @Override
    public PageResponse<AttachmentVO> queryPage(AttachmentQueryForm queryForm) {
        Page<?> page = PageConvertUtil.convert2PageQuery(queryForm);
        List<AttachmentVO> list = fileDao.queryPage(page, queryForm);

        // 为每个文件动态生成 fileUrl
        for (AttachmentVO fileVO : list) {
            if (fileVO.getFileKey() != null) {
                String fileUrl = fileStorageService.getFileUrl(fileVO.getFileKey()).getData();
                fileVO.setFileUrl(fileUrl);
            }
        }

        PageResponse<AttachmentVO> result = new PageResponse<>();
        result.setPageNum(page.getCurrent());
        result.setPageSize(page.getSize());
        result.setTotal(page.getTotal());
        result.setList(list);
        return result;
    }

    /**
     * 上传Base64格式的文件
     */
    @Override
    public String uploadBase64File(String base64Data, Integer folderType, String fileName, CurrentUser requestUser) {
        try {
            // 将Base64转换为MultipartFile
            byte[] fileBytes = Base64.getDecoder().decode(base64Data);
            MultipartFile multipartFile = createMultipartFile(fileBytes, fileName);

            // 使用现有的上传方法
            ApiResult<AttachmentUploadVO> response = fileUpload(multipartFile, folderType, requestUser);
            if (response.getOk()) {
                return response.getData().getFileUrl();
            } else {
                throw new BizException("上传Base64文件失败: " + response.getMsg());
            }
        } catch (Exception e) {
            log.error("上传Base64文件失败", e);
            throw new BizException("上传Base64文件失败: " + e.getMessage());
        }
    }

    /**
     * 高性能文件批量插入（与票券批量插入保持一致）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchInsertFiles(List<AttachmentEntity> fileEntities) {
        if (CollectionUtils.isEmpty(fileEntities)) {
            return 0;
        }

        log.info("开始批量插入文件记录，数量: {}", fileEntities.size());
        long startTime = System.currentTimeMillis();

        int totalInserted = 0;
        List<List<AttachmentEntity>> partitions = partition(fileEntities, BATCH_INSERT_SIZE);

        for (int i = 0; i < partitions.size(); i++) {
            List<AttachmentEntity> batch = partitions.get(i);

            try {
                // 使用 MyBatis 的批量插入
                int inserted = fileDao.insertBatch(batch);
                totalInserted += inserted;

                // 进度日志
                if (i % 10 == 0 || i == partitions.size() - 1) {
                    log.info("文件批量插入进度: {}/{}, 当前批次: {}, 累计: {}",
                            i + 1, partitions.size(), batch.size(), totalInserted);
                }
            } catch (Exception e) {
                log.error("文件批次插入失败，批次: {}, 错误: {}", i, e.getMessage());
                // 降级为单条插入
                for (AttachmentEntity entity : batch) {
                    try {
                        fileDao.insert(entity);
                        totalInserted++;
                    } catch (Exception ex) {
                        log.error("单条文件插入失败: {}", entity.getFileName(), ex);
                    }
                }
            }
        }

        long endTime = System.currentTimeMillis();
        log.info("文件批量插入完成，总数: {}, 成功: {}, 耗时: {}ms, 速度: {:.1f}条/秒",
                fileEntities.size(), totalInserted, endTime - startTime,
                totalInserted * 1000.0 / Math.max(1, endTime - startTime));

        return totalInserted;
    }

    /**
     * 创建二维码文件名 - 修正为批次编号_票券编号格式
     */
    public String createQRCodeFileName(String batchNo, String QuickBlueCode) {
        // 确保格式为：批次编号_票券编号.png
        String fileName = String.format("%s_%s.png", batchNo, QuickBlueCode);

        log.info("生成二维码文件名: {} -> {}",
                String.format("批次编号:%s, 票券编号:%s", batchNo, QuickBlueCode), fileName);

        return fileName;
    }

    /**
     * 上传票券二维码文件（批量优化版）
     */
    public String uploadQuickBlueQRCode(String QuickBlueId, String QuickBlueCode, String base64Data, CurrentUser requestUser) {
        String fileName = createQRCodeFileName(QuickBlueId, QuickBlueCode);
        return uploadBase64File(base64Data, AttachmentFolderTypeEnum.QuickBlue_QRCODE.getValue(), fileName, requestUser);
    }

    /**
     * 批量上传票券二维码文件（高性能优化版）- 支持真正的批量处理
     * 专门为票券二维码优化的批量上传
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, String> batchUploadQuickBlueQRCodes(List<QuickBlueQRCodeData> qrCodeDataList, CurrentUser requestUser) {
        if (CollectionUtils.isEmpty(qrCodeDataList)) {
            return new HashMap<>();
        }

        log.info("开始批量上传票券二维码，数量: {}", qrCodeDataList.size());
        long startTime = System.currentTimeMillis();

        try {
            // 准备文件数据
            List<Base64FileData> fileDataList = new ArrayList<>(qrCodeDataList.size());
            Map<String, Integer> QuickBlueIdToIndex = new HashMap<>();

            for (int i = 0; i < qrCodeDataList.size(); i++) {
                QuickBlueQRCodeData qrCodeData = qrCodeDataList.get(i);
                String fileName = createQRCodeFileName(qrCodeData.getQuickBlueId(), qrCodeData.getQuickBlueCode());
                Base64FileData fileData = new Base64FileData(fileName, qrCodeData.getBase64Data());
                fileDataList.add(fileData);
                QuickBlueIdToIndex.put(qrCodeData.getQuickBlueId(), i);
            }

            // 批量上传
            List<String> fileUrls = batchUploadBase64Files(fileDataList, AttachmentFolderTypeEnum.QuickBlue_QRCODE.getValue(), requestUser);

            // 建立映射关系
            Map<String, String> QuickBlueIdToFileUrl = new HashMap<>(qrCodeDataList.size());
            for (QuickBlueQRCodeData qrCodeData : qrCodeDataList) {
                Integer index = QuickBlueIdToIndex.get(qrCodeData.getQuickBlueId());
                if (index != null && index < fileUrls.size() && StringUtils.isNotBlank(fileUrls.get(index))) {
                    QuickBlueIdToFileUrl.put(qrCodeData.getQuickBlueId(), fileUrls.get(index));
                }
            }

            long endTime = System.currentTimeMillis();
            log.info("批量上传票券二维码完成，数量: {}，成功: {}，失败: {}，耗时: {}ms，速度: {:.1f}张/秒",
                    qrCodeDataList.size(), QuickBlueIdToFileUrl.size(),
                    qrCodeDataList.size() - QuickBlueIdToFileUrl.size(),
                    endTime - startTime,
                    QuickBlueIdToFileUrl.size() * 1000.0 / (endTime - startTime));

            return QuickBlueIdToFileUrl;

        } catch (Exception e) {
            log.error("批量上传票券二维码失败", e);
            throw new BizException("批量上传票券二维码失败: " + e.getMessage());
        }
    }

    /**
     * 删除票券二维码文件
     */
    public boolean deleteQuickBlueQRCode(String fileUrl) {
        try {
            // 从URL中提取fileKey
            String fileKey = extractFileKeyFromUrl(fileUrl);
            return deleteFile(fileKey);
        } catch (Exception e) {
            log.error("删除票券二维码文件失败: {}", fileUrl, e);
            return false;
        }
    }

    /**
     * 批量删除二维码文件（高性能优化版）
     */
    public int deleteQRCodeFiles(List<String> fileUrls, int batchSize) {
        if (CollectionUtils.isEmpty(fileUrls)) {
            return 0;
        }

        log.info("开始批量删除二维码文件，数量: {}，批次大小: {}", fileUrls.size(), batchSize);
        long startTime = System.currentTimeMillis();
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();

        try {
            // 使用并行流提高删除速度
            fileUrls.parallelStream().forEach(fileUrl -> {
                try {
                    if (deleteQuickBlueQRCode(fileUrl)) {
                        synchronized (this) {
                            successCount.getAndIncrement();
                        }
                    } else {
                        synchronized (this) {
                            failureCount.getAndIncrement();
                        }
                    }
                } catch (Exception e) {
                    synchronized (this) {
                        failureCount.getAndIncrement();
                    }
                    log.warn("删除文件失败: {}", fileUrl, e);
                }
            });

            long endTime = System.currentTimeMillis();
            log.info("批量删除二维码文件完成，成功: {}，失败: {}，总数: {}，耗时: {}ms，速度: {:.1f}个/秒",
                    successCount, failureCount, fileUrls.size(),
                    endTime - startTime,
                    fileUrls.size() * 1000.0 / (endTime - startTime));

            return successCount.get();

        } catch (Exception e) {
            log.error("批量删除二维码文件失败", e);
            return successCount.get();
        }
    }

    /**
     * 从URL中提取文件key
     */
    private String extractFileKeyFromUrl(String fileUrl) {
        if (StringUtils.isBlank(fileUrl)) {
            return "";
        }

        // 支持多种URL格式
        if (fileUrl.contains("?versionId=")) {
            // 处理带版本号的URL
            fileUrl = fileUrl.split("\\?")[0];
        }

        // 取最后一个斜杠后面的内容
        if (fileUrl.contains("/")) {
            String key = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            // 去除可能的查询参数
            if (key.contains("?")) {
                key = key.split("\\?")[0];
            }
            return key;
        }

        return fileUrl;
    }

    /**
     * 创建MultipartFile
     */
    private MultipartFile createMultipartFile(byte[] fileBytes, String fileName) {
        return new MultipartFile() {
            @Override
            public String getName() {
                return "file";
            }

            @Override
            public String getOriginalFilename() {
                return fileName;
            }

            @Override
            public String getContentType() {
                // 根据文件扩展名判断MIME类型
                if (fileName.toLowerCase().endsWith(".png")) {
                    return "image/png";
                } else if (fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".jpeg")) {
                    return "image/jpeg";
                } else if (fileName.toLowerCase().endsWith(".gif")) {
                    return "image/gif";
                } else {
                    return "application/octet-stream";
                }
            }

            @Override
            public boolean isEmpty() {
                return fileBytes == null || fileBytes.length == 0;
            }

            @Override
            public long getSize() {
                return fileBytes.length;
            }

            @Override
            public byte[] getBytes() throws IOException {
                return fileBytes;
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return new ByteArrayInputStream(fileBytes);
            }

            @Override
            public void transferTo(File dest) throws IOException, IllegalStateException {
                Files.write(dest.toPath(), fileBytes);
            }
        };
    }

    /**
     * 删除文件
     */
    @Override
    public boolean deleteFile(String fileKey) {
        if (StringUtils.isBlank(fileKey)) {
            return false;
        }

        try {
            ApiResult<String> response = fileStorageService.delete(fileKey);
            if (response.getOk()) {
                // 同时删除数据库记录
                fileDao.deleteByFileKey(fileKey);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("删除文件失败: {}", fileKey, e);
            return false;
        }
    }

    /**
     * 批量删除文件
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDeleteFiles(List<String> fileKeys) {
        if (CollectionUtils.isEmpty(fileKeys)) {
            return 0;
        }

        log.info("开始批量删除文件，数量: {}", fileKeys.size());
        long startTime = System.currentTimeMillis();
        int successCount = 0;

        try {
            // 批量删除存储服务中的文件
            for (String fileKey : fileKeys) {
                if (StringUtils.isNotBlank(fileKey)) {
                    try {
                        ApiResult<String> response = fileStorageService.delete(fileKey);
                        if (response.getOk()) {
                            successCount++;
                        }
                    } catch (Exception e) {
                        log.warn("删除存储文件失败: {}", fileKey, e);
                    }
                }
            }

            // 批量删除数据库记录
            if (successCount > 0) {
                String sql = "DELETE FROM t_file WHERE file_key IN (" +
                        String.join(",", Collections.nCopies(fileKeys.size(), "?")) + ")";
                int dbDeleted = jdbcTemplate.update(sql, fileKeys.toArray());
                log.info("批量删除数据库记录: {} 条", dbDeleted);
            }

            long endTime = System.currentTimeMillis();
            log.info("批量删除文件完成，成功: {}，总数: {}，耗时: {}ms",
                    successCount, fileKeys.size(), endTime - startTime);

            return successCount;

        } catch (Exception e) {
            log.error("批量删除文件失败", e);
            return successCount;
        }
    }

    /**
     * 检查文件是否存在
     */
    public boolean fileExists(String fileKey) {
        if (StringUtils.isBlank(fileKey)) {
            return false;
        }

        try {
            AttachmentVO fileVO = fileDao.getByFileKey(fileKey);
            return fileVO != null;
        } catch (Exception e) {
            log.error("检查文件存在性失败: {}", fileKey, e);
            return false;
        }
    }

    /**
     * 批量检查文件是否存在
     */
    public Map<String, Boolean> batchFileExists(List<String> fileKeys) {
        Map<String, Boolean> result = new HashMap<>();

        if (CollectionUtils.isEmpty(fileKeys)) {
            return result;
        }

        try {
            // 批量查询
            List<AttachmentVO> fileList = fileDao.selectByFileKeyList(new HashSet<>(fileKeys));
            Set<String> existingKeys = fileList.stream()
                    .map(AttachmentVO::getFileKey)
                    .collect(Collectors.toSet());

            for (String fileKey : fileKeys) {
                result.put(fileKey, existingKeys.contains(fileKey));
            }

            log.debug("批量检查文件存在性完成，数量: {}", fileKeys.size());

        } catch (Exception e) {
            log.error("批量检查文件存在性失败", e);
            // 降级为单条检查
            for (String fileKey : fileKeys) {
                try {
                    result.put(fileKey, fileExists(fileKey));
                } catch (Exception ex) {
                    result.put(fileKey, false);
                }
            }
        }

        return result;
    }

    /**
     * 清洗Base64字符串 - 移除data URI前缀和非法字符
     */
    private String cleanBase64Data(String base64Data) {
        if (StringUtils.isBlank(base64Data)) {
            return "";
        }

        String cleaned = base64Data.trim();

        // 移除data URI前缀 (如: data:image/png;base64,)
        if (cleaned.startsWith("data:")) {
            int base64Index = cleaned.indexOf("base64,");
            if (base64Index > 0) {
                cleaned = cleaned.substring(base64Index + 7);
            }
        }

        // 移除所有非Base64字符（允许的字符：A-Z, a-z, 0-9, +, /, =）
        cleaned = cleaned.replaceAll("[^A-Za-z0-9+/=]", "");

        // 检查长度是否为4的倍数（Base64要求）
        int remainder = cleaned.length() % 4;
        if (remainder != 0) {
            // 补充等号填充
            cleaned += "=".repeat(4 - remainder);
        }

        return cleaned;
    }

    /**
     * 验证是否为有效的Base64字符串
     */
    private boolean isValidBase64(String base64Data) {
        if (StringUtils.isBlank(base64Data)) {
            return false;
        }

        // 检查是否包含非法字符
        if (!base64Data.matches("^[A-Za-z0-9+/=]+$")) {
            return false;
        }

        // 长度检查
        if (base64Data.length() < 4) {
            return false;
        }

        try {
            // 尝试解码验证
            Base64.getDecoder().decode(base64Data);
            return true;
        } catch (Exception e) {
            log.debug("Base64验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 高性能批量上传Base64格式文件 - 优化版
     */
    @Transactional(rollbackFor = Exception.class)
    public List<String> batchUploadBase64Files(List<Base64FileData> fileDataList, Integer folderType, CurrentUser requestUser) {
        if (CollectionUtils.isEmpty(fileDataList)) {
            return new ArrayList<>();
        }

        AttachmentFolderTypeEnum folderTypeEnum = EnumValueUtil.getEnumByValue(folderType, AttachmentFolderTypeEnum.class);
        if (null == folderTypeEnum) {
            throw new BizException("文件夹类型错误");
        }

        log.info("开始批量上传Base64文件，数量: {}", fileDataList.size());
        long totalStartTime = System.currentTimeMillis();

        // 批量准备数据
        List<AttachmentEntity> fileEntities = new ArrayList<>(fileDataList.size());
        List<String> fileUrls = new ArrayList<>(fileDataList.size());
        List<String> fileKeys = new ArrayList<>(fileDataList.size());

        try {
            // 第一步：批量上传到存储服务
            long uploadStartTime = System.currentTimeMillis();
            int uploadSuccessCount = 0;

            for (int i = 0; i < fileDataList.size(); i++) {
                Base64FileData fileData = fileDataList.get(i);

                try {
                    // 清洗和验证Base64数据
                    String cleanedBase64 = cleanBase64Data(fileData.getBase64Data());
                    if (StringUtils.isBlank(cleanedBase64)) {
                        log.error("Base64数据为空，文件名: {}", fileData.getFileName());
                        fileUrls.add("");  // 添加空字符串占位
                        fileKeys.add("");
                        continue;
                    }

                    if (!isValidBase64(cleanedBase64)) {
                        log.error("Base64数据格式无效，文件名: {}", fileData.getFileName());
                        fileUrls.add("");
                        fileKeys.add("");
                        continue;
                    }

                    byte[] fileBytes = Base64.getDecoder().decode(cleanedBase64);
                    MultipartFile multipartFile = createMultipartFile(fileBytes, fileData.getFileName());

                    // 上传到存储服务
                    ApiResult<AttachmentUploadVO> response = fileStorageService.upload(multipartFile, folderTypeEnum.getFolder());
                    if (!response.getOk()) {
                        log.error("文件上传失败: {}，文件名: {}", response.getMsg(), fileData.getFileName());
                        // 跳过失败的文件，继续处理其他文件
                        fileUrls.add("");  // 添加空字符串占位
                        fileKeys.add("");
                        continue;
                    }

                    // 创建文件实体
                    AttachmentEntity fileEntity = new AttachmentEntity();
                    fileEntity.setFolderType(folderTypeEnum.getValue());
                    fileEntity.setFileName(fileData.getFileName());
                    fileEntity.setFileSize((long) fileBytes.length);
                    fileEntity.setFileKey(response.getData().getFileKey());
                    fileEntity.setFileType("image/png");
                    fileEntity.setCreatorId(requestUser == null ? null : requestUser.getUserId());
                    fileEntity.setCreatorName(requestUser == null ? null : requestUser.getUserName());
                    fileEntity.setCreatorUserType(requestUser == null ? null : requestUser.getUserType());
                    fileEntity.setCreateTime(LocalDateTime.now());
                    fileEntity.setUpdateTime(LocalDateTime.now());

                    fileEntities.add(fileEntity);
                    fileUrls.add(response.getData().getFileUrl());
                    fileKeys.add(response.getData().getFileKey());
                    uploadSuccessCount++;

                } catch (Exception e) {
                    log.error("处理Base64文件数据失败: {}", fileData.getFileName(), e);
                    // 跳过失败的文件，继续处理其他文件
                    fileUrls.add("");  // 添加空字符串占位
                    fileKeys.add("");
                }
            }

            long uploadEndTime = System.currentTimeMillis();
            log.info("文件存储服务上传完成，成功: {}，失败: {}，耗时: {}ms",
                    uploadSuccessCount, fileDataList.size() - uploadSuccessCount,
                    uploadEndTime - uploadStartTime);

            // 第二步：批量插入数据库（使用新的批量插入方法）
            if (!fileEntities.isEmpty()) {
                long dbStartTime = System.currentTimeMillis();

                try {
                    // 使用新的批量插入方法
                    int inserted = batchInsertFiles(fileEntities);
                    log.info("批量插入文件记录完成，期望: {}，实际: {}", fileEntities.size(), inserted);

                } catch (Exception e) {
                    log.error("批量插入失败，尝试分批次降级处理", e);
                    // 降级为分批次处理
                    int fallbackSuccess = fallbackBatchInsert(fileEntities);
                    log.warn("降级插入完成，成功: {}，总数: {}", fallbackSuccess, fileEntities.size());
                }

                long dbEndTime = System.currentTimeMillis();
                log.info("数据库插入完成，数量: {}，耗时: {}ms",
                        fileEntities.size(), dbEndTime - dbStartTime);
            }

            long totalEndTime = System.currentTimeMillis();
            log.info("批量上传文件全部完成，总数: {}，上传成功: {}，数据库插入: {}，总耗时: {}ms",
                    fileDataList.size(), uploadSuccessCount, fileEntities.size(),
                    totalEndTime - totalStartTime);

            return fileUrls;

        } catch (Exception e) {
            log.error("批量上传文件失败", e);
            // 回滚：删除已上传的文件
            rollbackUploadedFiles(fileKeys);
            throw new BizException("批量上传文件失败: " + e.getMessage());
        }
    }

    /**
     * 高性能JDBC批量插入
     */
    private void batchInsertFileEntitiesJDBC(List<AttachmentEntity> fileEntities) {
        if (CollectionUtils.isEmpty(fileEntities)) {
            return;
        }

        // 批量插入SQL - 根据表结构调整字段
        String sql = "INSERT INTO t_file (folder_type, file_name, file_size, " +
                "file_key, file_type, creator_id, creator_name, creator_user_type, " +
                "create_time, update_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            // 分批次处理，每批JDBC_BATCH_SIZE条
            for (int i = 0; i < fileEntities.size(); i += JDBC_BATCH_SIZE) {
                int endIndex = Math.min(i + JDBC_BATCH_SIZE, fileEntities.size());
                List<AttachmentEntity> batch = fileEntities.subList(i, endIndex);

                jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int index) throws SQLException {
                        AttachmentEntity entity = batch.get(index);

                        // 设置参数，注意顺序要和SQL中的占位符对应
                        ps.setInt(1, entity.getFolderType() != null ? entity.getFolderType() : 1);
                        ps.setString(2, entity.getFileName());
                        ps.setLong(3, entity.getFileSize() != null ? entity.getFileSize() : 0);
                        ps.setString(4, entity.getFileKey());
                        ps.setString(5, entity.getFileType() != null ? entity.getFileType() : "image/png");

                        // 处理可能为空的字段
                        if (entity.getCreatorId() != null) {
                            ps.setLong(6, entity.getCreatorId());
                        } else {
                            ps.setNull(6, java.sql.Types.BIGINT);
                        }

                        ps.setString(7, entity.getCreatorName());

                        if (entity.getCreatorUserType() != null) {
                            ps.setInt(8, entity.getCreatorUserType());
                        } else {
                            ps.setNull(8, java.sql.Types.INTEGER);
                        }

                        ps.setTimestamp(9, java.sql.Timestamp.valueOf(entity.getCreateTime()));
                        ps.setTimestamp(10, java.sql.Timestamp.valueOf(entity.getUpdateTime()));
                    }

                    @Override
                    public int getBatchSize() {
                        return batch.size();
                    }
                });

                if (i % 2000 == 0) {
                    log.debug("已批量插入 {} 条记录，进度: {}/{}", batch.size(), i + batch.size(), fileEntities.size());
                }
            }

            log.info("JDBC批量插入完成，总数量: {}", fileEntities.size());

        } catch (Exception e) {
            log.error("JDBC批量插入失败", e);
            throw new BizException("JDBC批量插入失败: " + e.getMessage());
        }
    }

    /**
     * 降级方案：分批插入
     */
    private int fallbackBatchInsert(List<AttachmentEntity> fileEntities) {
        if (CollectionUtils.isEmpty(fileEntities)) {
            return 0;
        }

        int successCount = 0;

        for (int i = 0; i < fileEntities.size(); i += FALLBACK_BATCH_SIZE) {
            int endIndex = Math.min(i + FALLBACK_BATCH_SIZE, fileEntities.size());
            List<AttachmentEntity> batch = fileEntities.subList(i, endIndex);

            try {
                for (AttachmentEntity entity : batch) {
                    try {
                        fileDao.insert(entity);
                        successCount++;
                    } catch (Exception e) {
                        log.error("单条插入失败: {}", entity.getFileName(), e);
                    }
                }

                if (successCount % LOG_INTERVAL == 0) {
                    log.debug("降级插入进度: {}/{}", successCount, fileEntities.size());
                }

            } catch (Exception e) {
                log.error("批次插入失败，范围: {}-{}", i, endIndex - 1, e);
            }
        }

        log.warn("批量插入降级完成，成功: {}，总数: {}", successCount, fileEntities.size());
        return successCount;
    }

    /**
     * 回滚已上传的文件
     */
    private void rollbackUploadedFiles(List<String> fileKeys) {
        if (CollectionUtils.isEmpty(fileKeys)) {
            return;
        }

        int deletedCount = 0;
        log.info("开始回滚已上传的文件，数量: {}", fileKeys.size());

        for (String fileKey : fileKeys) {
            if (StringUtils.isNotBlank(fileKey)) {
                try {
                    fileStorageService.delete(fileKey);
                    deletedCount++;
                } catch (Exception e) {
                    log.warn("删除文件失败: {}", fileKey, e);
                }
            }
        }

        log.info("回滚文件完成，成功删除: {} 个文件", deletedCount);
    }

    /**
     * 列表分片工具方法
     */
    private <T> List<List<T>> partition(List<T> list, int size) {
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            int end = Math.min(i + size, list.size());
            partitions.add(list.subList(i, end));
        }
        return partitions;
    }

    /**
     * 准备文件实体（设置默认值等）
     */
    private void prepareFileEntities(List<AttachmentEntity> fileEntities) {
        LocalDateTime now = LocalDateTime.now();

        for (AttachmentEntity entity : fileEntities) {
            // 确保必要的字段有值
            if (entity.getCreateTime() == null) {
                entity.setCreateTime(now);
            }
            if (entity.getUpdateTime() == null) {
                entity.setUpdateTime(now);
            }
            if (entity.getFileSize() == null) {
                entity.setFileSize(0L);
            }
            if (entity.getFolderType() == null) {
                entity.setFolderType(1); // 默认通用文件夹
            }
            if (StringUtils.isBlank(entity.getFileType())) {
                // 根据文件名推断文件类型
                if (entity.getFileName() != null) {
                    if (entity.getFileName().toLowerCase().endsWith(".png")) {
                        entity.setFileType("image/png");
                    } else if (entity.getFileName().toLowerCase().endsWith(".jpg") ||
                            entity.getFileName().toLowerCase().endsWith(".jpeg")) {
                        entity.setFileType("image/jpeg");
                    } else if (entity.getFileName().toLowerCase().endsWith(".gif")) {
                        entity.setFileType("image/gif");
                    } else {
                        entity.setFileType("application/octet-stream");
                    }
                } else {
                    entity.setFileType("application/octet-stream");
                }
            }
        }
    }

    /**
     * Base64文件数据内部类
     */
    @Data
    public static class Base64FileData {
        private String fileName;
        private String base64Data;

        public Base64FileData(String fileName, String base64Data) {
            this.fileName = fileName;
            this.base64Data = base64Data;
        }
    }

    /**
     * 票券二维码数据内部类
     */
    @Data
    public static class QuickBlueQRCodeData {
        private String QuickBlueId;
        private String QuickBlueCode;
        private String base64Data;

        public QuickBlueQRCodeData(String QuickBlueId, String QuickBlueCode, String base64Data) {
            this.QuickBlueId = QuickBlueId;
            this.QuickBlueCode = QuickBlueCode;
            this.base64Data = base64Data;
        }
    }

    /**
     * 性能统计信息
     */
    @Data
    public static class PerformanceStats {
        private int totalFiles;
        private int successFiles;
        private int failedFiles;
        private long totalTimeMs;
        private double avgTimePerFileMs;
        private double speedPerSecond;

        @Override
        public String toString() {
            return String.format("PerformanceStats{total=%d, success=%d, failed=%d, " +
                            "time=%.2fs, avg=%.2fms/文件, speed=%.1f}个/秒",
                    totalFiles, successFiles, failedFiles,
                    totalTimeMs / 1000.0, avgTimePerFileMs, speedPerSecond);
        }
    }

    /**
     * 批量插入统计信息
     */
    @Data
    public static class BatchInsertStats {
        private int totalFiles;
        private int successFiles;
        private int failedFiles;
        private long totalTimeMs;
        private long dbInsertTimeMs;
        private long fileUploadTimeMs;
        private double avgTimePerFileMs;
        private double dbSpeedPerSecond;
        private double totalSpeedPerSecond;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Map<String, Object> batchDetails;

        public BatchInsertStats() {}

        public BatchInsertStats(int totalFiles, int successFiles, int failedFiles,
                                long totalTimeMs, long dbInsertTimeMs) {
            this.totalFiles = totalFiles;
            this.successFiles = successFiles;
            this.failedFiles = failedFiles;
            this.totalTimeMs = totalTimeMs;
            this.dbInsertTimeMs = dbInsertTimeMs;
        }

        @Override
        public String toString() {
            return String.format("BatchInsertStats{total=%d, success=%d, failed=%d, " +
                            "totalTime=%.2fs, dbTime=%.2fs, uploadTime=%.2fs, " +
                            "totalSpeed=%.1f}个/秒, dbSpeed=%.1f}个/秒",
                    totalFiles, successFiles, failedFiles,
                    totalTimeMs / 1000.0, dbInsertTimeMs / 1000.0, fileUploadTimeMs / 1000.0,
                    totalSpeedPerSecond, dbSpeedPerSecond);
        }
    }
}
