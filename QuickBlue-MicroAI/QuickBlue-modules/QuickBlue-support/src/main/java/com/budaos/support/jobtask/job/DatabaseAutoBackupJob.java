package com.budaos.support.jobtask.job;

import com.budaos.support.dao.DatabaseBackupConfigDao;
import com.budaos.support.dao.DatabaseBackupDao;
import com.budaos.support.domain.entity.DatabaseBackupConfigEntity;
import com.budaos.support.domain.entity.DatabaseBackupEntity;
import com.budaos.support.jobtask.core.JobTask;
import com.budaos.support.util.DatabaseBackupUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 数据库自动备份定时任务
 */
@Slf4j
@Component
public class DatabaseAutoBackupJob implements JobTask {

    @Resource
    private DatabaseBackupDao databaseBackupDao;

    @Resource
    private DatabaseBackupConfigDao databaseBackupConfigDao;

    @Value("${spring.datasource.url:}")
    private String defaultDbUrl;

    @Value("${spring.datasource.username:}")
    private String defaultDbUsername;

    @Value("${spring.datasource.password:}")
    private String defaultDbPassword;

    @Value("${backup.path:./backups}")
    private String defaultBackupPath;

    @Override
    public Integer getJobId() {
        return 6;
    }

    @Override
    public String run(String param) {
        log.info("开始执行数据库自动备份任务");

        try {
            // 解析数据库连接信息
            String dbHost = parseHostFromUrl(defaultDbUrl);
            Integer dbPort = parsePortFromUrl(defaultDbUrl);
            String dbName = parseDbName(defaultDbUrl);
            String dbUsername = defaultDbUsername;
            String dbPassword = defaultDbPassword;

            // 获取备份配置（如果配置表存在）
            DatabaseBackupConfigEntity configEntity = null;
            try {
                configEntity = databaseBackupConfigDao.selectById(1);
            } catch (Exception e) {
                log.debug("数据库备份配置表不存在或查询失败，使用默认配置");
            }

            // 如果存在配置且未启用自动备份，则跳过
            if (configEntity != null && !Boolean.TRUE.equals(configEntity.getAutoBackupEnabled())) {
                log.info("自动备份未启用，跳过备份");
                return "自动备份未启用，跳过备份";
            }

            // 使用配置中的值或默认值
            String backupPath = (configEntity != null && configEntity.getBackupPath() != null)
                    ? configEntity.getBackupPath() : defaultBackupPath;
            String mysqldumpPath = (configEntity != null) ? configEntity.getMysqldumpPath() : null;

            // 如果配置中有数据库信息，优先使用配置值
            if (configEntity != null) {
                if (configEntity.getDbHost() != null) dbHost = configEntity.getDbHost();
                if (configEntity.getDbPort() != null) dbPort = configEntity.getDbPort();
                if (configEntity.getDbName() != null) dbName = configEntity.getDbName();
                if (configEntity.getDbUsername() != null) dbUsername = configEntity.getDbUsername();
                if (configEntity.getDbPassword() != null) dbPassword = configEntity.getDbPassword();
            }

            log.info("备份数据库: {}@{}:{}/{}", dbUsername, dbHost, dbPort, dbName);

            // 创建备份记录
            DatabaseBackupEntity backupEntity = new DatabaseBackupEntity();
            backupEntity.setFileName("备份中...");
            backupEntity.setFilePath("");
            backupEntity.setBackupType(DatabaseBackupUtil.BackupType.AUTO.getValue());
            backupEntity.setBackupStatus(DatabaseBackupUtil.BackupStatus.BACKUPING.getValue());
            backupEntity.setRemark("自动备份");
            backupEntity.setOperator("系统");
            backupEntity.setCreateTime(LocalDateTime.now());
            databaseBackupDao.insert(backupEntity);

            // 执行备份
            DatabaseBackupUtil.BackupResult result = DatabaseBackupUtil.backupDatabase(
                    dbHost,
                    dbPort,
                    dbName,
                    dbUsername,
                    dbPassword,
                    backupPath,
                    mysqldumpPath
            );

            // 更新备份结果
            DatabaseBackupEntity updateEntity = new DatabaseBackupEntity();
            updateEntity.setBackupId(backupEntity.getBackupId());
            updateEntity.setFileName(result.getFileName());
            updateEntity.setFilePath(result.getFilePath());
            updateEntity.setFileSize(result.getFileSize());
            updateEntity.setBackupStatus(result.getSuccess() ?
                    DatabaseBackupUtil.BackupStatus.SUCCESS.getValue() :
                    DatabaseBackupUtil.BackupStatus.FAILED.getValue());
            updateEntity.setErrorMessage(result.getErrorMessage());
            databaseBackupDao.updateById(updateEntity);

            if (result.getSuccess()) {
                log.info("数据库自动备份成功，备份ID: {}, 文件: {}", backupEntity.getBackupId(), result.getFileName());
                return "数据库备份成功: " + result.getFileName();
            } else {
                log.error("数据库自动备份失败: {}", result.getErrorMessage());
                return "数据库备份失败: " + result.getErrorMessage();
            }

        } catch (Exception e) {
            log.error("数据库自动备份异常", e);
            return "数据库备份异常: " + e.getMessage();
        }
    }

    /**
     * 从JDBC URL解析数据库名
     */
    private String parseDbName(String url) {
        if (url == null || !url.startsWith("jdbc:mysql://")) {
            return "QuickBlue";
        }
        try {
            String[] parts = url.substring(13).split("/");
            if (parts.length >= 2) {
                String dbNameWithParams = parts[1];
                return dbNameWithParams.split("\\?")[0];
            }
        } catch (Exception e) {
            log.debug("解析数据库名失败", e);
        }
        return "QuickBlue";
    }

    /**
     * 从JDBC URL解析主机地址
     */
    private String parseHostFromUrl(String url) {
        if (url == null || !url.startsWith("jdbc:mysql://")) {
            return "localhost";
        }
        try {
            String[] parts = url.substring(13).split("/");
            String hostPort = parts[0];
            if (hostPort.contains(":")) {
                return hostPort.split(":")[0];
            }
            return hostPort;
        } catch (Exception e) {
            log.debug("解析主机地址失败", e);
        }
        return "localhost";
    }

    /**
     * 从JDBC URL解析端口
     */
    private Integer parsePortFromUrl(String url) {
        if (url == null || !url.startsWith("jdbc:mysql://")) {
            return 3306;
        }
        try {
            String[] parts = url.substring(13).split("/");
            String hostPort = parts[0];
            if (hostPort.contains(":")) {
                return Integer.parseInt(hostPort.split(":")[1]);
            }
        } catch (Exception e) {
            log.debug("解析端口失败", e);
        }
        return 3306;
    }
}
