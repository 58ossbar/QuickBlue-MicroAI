package com.budaos.support.controller;

import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.CurrentUser;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.common.core.util.RequestContextUtil;
import com.budaos.common.web.annotation.AuditLog;
import com.budaos.support.domain.form.DatabaseBackupAddForm;
import com.budaos.support.domain.form.DatabaseBackupConfigUpdateForm;
import com.budaos.support.domain.form.DatabaseBackupQueryForm;
import com.budaos.support.domain.entity.DatabaseBackupEntity;
import com.budaos.support.domain.vo.DatabaseBackupVO;
import com.budaos.support.domain.vo.DatabaseBackupConfigVO;
import com.budaos.support.service.DatabaseBackupService;
import com.budaos.support.util.DatabaseBackupUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 数据库备份控制器
 */
@Slf4j
@Tag(name = "数据库备份")
@RestController
@RequestMapping("/database-backup")
@AuditLog(module = "数据库备份", description = "数据库备份操作")
public class DatabaseBackupController {

    @Resource
    private DatabaseBackupService databaseBackupService;

    @Operation(summary = "分页查询备份记录")
    @PostMapping("/queryPage")
    public ApiResult<PageResponse<DatabaseBackupVO>> queryPage(@RequestBody @Valid DatabaseBackupQueryForm queryForm) {
        return databaseBackupService.queryBackupPage(queryForm);
    }

    @Operation(summary = "手动备份")
    @PostMapping("/manualBackup")
    public ApiResult<String> manualBackup(@RequestBody @Valid DatabaseBackupAddForm addForm) {
        CurrentUser requestUser = RequestContextUtil.getRequestUser();
        return databaseBackupService.manualBackup(addForm, requestUser.getUserName());
    }

    @Operation(summary = "删除备份记录")
    @PostMapping("/delete")
    public ApiResult<String> delete(@RequestParam Long backupId) {
        return databaseBackupService.deleteBackup(backupId);
    }

    @Operation(summary = "查询备份配置")
    @GetMapping("/config/get")
    public ApiResult<DatabaseBackupConfigVO> getConfig() {
        return databaseBackupService.getConfig();
    }

    @Operation(summary = "更新备份配置")
    @PostMapping("/config/update")
    public ApiResult<String> updateConfig(@RequestBody @Valid DatabaseBackupConfigUpdateForm updateForm) {
        return databaseBackupService.updateConfig(updateForm);
    }

    @Operation(summary = "清理过期备份")
    @PostMapping("/cleanExpired")
    public ApiResult<String> cleanExpired() {
        return databaseBackupService.cleanExpiredBackups();
    }

    @Operation(summary = "下载备份文件")
    @GetMapping("/download")
    public ResponseEntity<org.springframework.core.io.Resource> downloadBackup(@RequestParam Long backupId) {
        // 获取备份记录
        DatabaseBackupEntity backupEntity = databaseBackupService.getBackupById(backupId);
        if (backupEntity == null) {
            return ResponseEntity.notFound().build();
        }

        String filePath = backupEntity.getFilePath();
        if (filePath == null || filePath.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        File file = new File(filePath);
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        org.springframework.core.io.Resource resource = new FileSystemResource(file);

        try {
            // 获取文件名并进行URL编码
            String fileName = backupEntity.getFileName();
            if (fileName == null || fileName.isEmpty()) {
                fileName = file.getName();
            }
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(file.length())
                    .body(resource);
        } catch (Exception e) {
            log.error("下载备份文件失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
