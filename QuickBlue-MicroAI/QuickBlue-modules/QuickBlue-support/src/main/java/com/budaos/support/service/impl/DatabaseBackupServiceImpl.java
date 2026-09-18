package com.budaos.support.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.support.dao.DatabaseBackupConfigDao;
import com.budaos.support.dao.DatabaseBackupDao;
import com.budaos.support.domain.entity.DatabaseBackupConfigEntity;
import com.budaos.support.domain.entity.DatabaseBackupEntity;
import com.budaos.support.domain.form.DatabaseBackupAddForm;
import com.budaos.support.domain.form.DatabaseBackupConfigUpdateForm;
import com.budaos.support.domain.form.DatabaseBackupQueryForm;
import com.budaos.support.domain.vo.DatabaseBackupConfigVO;
import com.budaos.support.domain.vo.DatabaseBackupVO;
import com.budaos.support.service.DatabaseBackupService;
import com.budaos.common.core.util.BeanCopyUtil;
import com.budaos.common.core.util.PageConvertUtil;
import com.budaos.support.util.DatabaseBackupUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据库备份服务实现（简化版）
 *
 * @author budaos
 */
@Slf4j
@Service
public class DatabaseBackupServiceImpl extends ServiceImpl<DatabaseBackupDao, DatabaseBackupEntity> implements DatabaseBackupService {

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

    @Override
    public ApiResult<PageResponse<DatabaseBackupVO>> queryBackupPage(DatabaseBackupQueryForm queryForm) {
        Page<DatabaseBackupEntity> page = PageConvertUtil.convert2PageQuery(queryForm);
        List<DatabaseBackupEntity> entityList = databaseBackupDao.queryByPage(page, queryForm);
        List<DatabaseBackupVO> voList = BeanCopyUtil.copyList(entityList, DatabaseBackupVO.class);
        PageResponse<DatabaseBackupVO> pageResult = new PageResponse<>();
        pageResult.setPageNum(page.getCurrent());
        pageResult.setPageSize(page.getSize());
        pageResult.setTotal(page.getTotal());
        pageResult.setList(voList);
        // 处理显示字段
        if (pageResult.getList() != null) {
            pageResult.getList().forEach(this::fillDisplayFields);
        }
        return ApiResult.ok(pageResult);
    }

    @Override
    public DatabaseBackupEntity getBackupById(Long backupId) {
        return databaseBackupDao.selectById(backupId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<String> manualBackup(DatabaseBackupAddForm addForm, String requestUser) {
        // 获取备份配置
        DatabaseBackupConfigEntity configEntity = databaseBackupConfigDao.selectOne(null);
        if (configEntity == null) {
            return ApiResult.userErrorParam("请先配置数据库备份信息");
        }

        // 创建备份记录
        DatabaseBackupEntity backupEntity = new DatabaseBackupEntity();
        backupEntity.setFileName("备份中...");
        backupEntity.setFilePath("");
        backupEntity.setBackupType(2); // 手动备份
        backupEntity.setBackupStatus(0); // 备份中
        backupEntity.setRemark(addForm.getRemark());
        backupEntity.setOperator(requestUser);
        backupEntity.setCreateTime(LocalDateTime.now());
        databaseBackupDao.insert(backupEntity);

        // 异步执行备份
        asyncExecuteBackup(backupEntity.getBackupId(), configEntity, requestUser);

        return ApiResult.ok("备份任务已提交，正在后台执行");
    }

    /**
     * 异步执行备份
     */
    private void asyncExecuteBackup(Long backupId, DatabaseBackupConfigEntity configEntity, String operator) {
        new Thread(() -> {
            try {
                DatabaseBackupUtil.BackupResult result = DatabaseBackupUtil.backupDatabase(
                        configEntity.getDbHost(),
                        configEntity.getDbPort(),
                        configEntity.getDbName(),
                        configEntity.getDbUsername(),
                        configEntity.getDbPassword(),
                        configEntity.getBackupPath(),
                        configEntity.getMysqldumpPath()
                );

                // 更新备份结果
                DatabaseBackupEntity updateEntity = new DatabaseBackupEntity();
                updateEntity.setBackupId(backupId);
                updateEntity.setFileName(result.getFileName());
                updateEntity.setFilePath(result.getFilePath());
                updateEntity.setFileSize(result.getFileSize());
                updateEntity.setBackupStatus(result.getSuccess() ?
                        1 : 2);
                updateEntity.setErrorMessage(result.getErrorMessage());
                databaseBackupDao.updateById(updateEntity);

                log.info("数据库备份完成，备份ID: {}, 结果: {}", backupId, result.getSuccess() ? "成功" : "失败");

            } catch (Exception e) {
                log.error("数据库备份异常", e);
                DatabaseBackupEntity updateEntity = new DatabaseBackupEntity();
                updateEntity.setBackupId(backupId);
                updateEntity.setBackupStatus(2);
                updateEntity.setErrorMessage("备份异常: " + e.getMessage());
                databaseBackupDao.updateById(updateEntity);
            }
        }).start();
    }

    @Override
    public ApiResult<String> deleteBackup(Long backupId) {
        DatabaseBackupEntity backupEntity = databaseBackupDao.selectById(backupId);
        if (backupEntity == null) {
            return ApiResult.userErrorParam("数据不存在");
        }

        // 删除备份文件
        if (StrUtil.isNotBlank(backupEntity.getFilePath())) {
            try {
                java.io.File file = new java.io.File(backupEntity.getFilePath());
                if (file.exists()) {
                    file.delete();
                    log.info("删除备份文件: {}", backupEntity.getFilePath());
                }
            } catch (Exception e) {
                log.error("删除备份文件失败", e);
            }
        }

        // 删除记录
        databaseBackupDao.deleteById(backupId);
        return ApiResult.ok();
    }

    @Override
    public ApiResult<DatabaseBackupConfigVO> getConfig() {
        DatabaseBackupConfigEntity configEntity = databaseBackupConfigDao.selectOne(null);

        DatabaseBackupConfigVO configVO;
        if (configEntity == null) {
            // 如果数据库中没有配置，使用Spring配置的默认值
            configVO = new DatabaseBackupConfigVO();
            configVO.setDbHost(parseHostFromUrl(defaultDbUrl));
            configVO.setDbPort(parsePortFromUrl(defaultDbUrl));
            configVO.setDbName(parseDbNameFromUrl(defaultDbUrl));
            configVO.setDbUsername(defaultDbUsername);
            configVO.setDbPassword(defaultDbPassword);
            configVO.setBackupPath("/data/backup");
            configVO.setAutoBackupEnabled(false);
            configVO.setAutoBackupCron("0 0 2 * * ?");
            configVO.setRetentionDays(30);
        } else {
            configVO = BeanCopyUtil.copyProperties(configEntity, DatabaseBackupConfigVO.class);
        }

        return ApiResult.ok(configVO);
    }

    @Override
    public ApiResult<String> updateConfig(DatabaseBackupConfigUpdateForm updateForm) {
        DatabaseBackupConfigEntity configEntity = databaseBackupConfigDao.selectOne(null);
        if (configEntity == null) {
            // 插入新配置
            configEntity = BeanCopyUtil.copyProperties(updateForm, DatabaseBackupConfigEntity.class);
            databaseBackupConfigDao.insert(configEntity);
        } else {
            // 更新配置
            updateForm.setConfigId(configEntity.getConfigId());
            configEntity = BeanCopyUtil.copyProperties(updateForm, DatabaseBackupConfigEntity.class);
            configEntity.setUpdateTime(LocalDateTime.now());
            databaseBackupConfigDao.updateById(configEntity);
        }
        return ApiResult.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<String> cleanExpiredBackups() {
        DatabaseBackupConfigEntity configEntity = databaseBackupConfigDao.selectOne(null);
        if (configEntity == null || configEntity.getRetentionDays() == null) {
            return ApiResult.userErrorParam("未配置保留天数");
        }

        // 计算过期时间
        LocalDateTime expireTime = LocalDateTime.now().minusDays(configEntity.getRetentionDays());

        // 查询过期的备份记录
        Page<DatabaseBackupEntity> page = new Page<>(1, 1000); // 获取最多1000条记录
        DatabaseBackupQueryForm queryForm = new DatabaseBackupQueryForm();
        queryForm.setCreateTimeBefore(expireTime);

        List<DatabaseBackupEntity> expiredBackups = databaseBackupDao.queryByPage(page, queryForm);

        if (expiredBackups == null || expiredBackups.isEmpty()) {
            return ApiResult.ok("没有需要清理的过期备份");
        }

        int deletedCount = 0;
        for (DatabaseBackupEntity backup : expiredBackups) {
            // 删除备份文件
            if (StrUtil.isNotBlank(backup.getFilePath())) {
                try {
                    java.io.File file = new java.io.File(backup.getFilePath());
                    if (file.exists()) {
                        file.delete();
                        log.info("删除过期备份文件: {}", backup.getFilePath());
                    }
                } catch (Exception e) {
                    log.error("删除备份文件失败", e);
                }
            }

            // 删除记录
            databaseBackupDao.deleteById(backup.getBackupId());
            deletedCount++;
        }

        return ApiResult.ok(String.format("已清理 %d 个过期备份", deletedCount));
    }

    /**
     * 填充显示字段
     */
    private void fillDisplayFields(DatabaseBackupVO vo) {
        // 备份类型名称
        if (vo.getBackupType() != null) {
            if (1 == vo.getBackupType()) {
                vo.setBackupTypeName("自动备份");
            } else if (2 == vo.getBackupType()) {
                vo.setBackupTypeName("手动备份");
            }
        }

        // 备份状态名称
        if (vo.getBackupStatus() != null) {
            if (0 == vo.getBackupStatus()) {
                vo.setBackupStatusName("备份中");
            } else if (1 == vo.getBackupStatus()) {
                vo.setBackupStatusName("备份成功");
            } else if (2 == vo.getBackupStatus()) {
                vo.setBackupStatusName("备份失败");
            }
        }

        // 文件大小显示
        if (vo.getFileSize() != null) {
            vo.setFileSizeDisplay(formatFileSize(vo.getFileSize()));
        }
    }

    /**
     * 格式化文件大小
     */
    private String formatFileSize(long size) {
        if (size <= 0) {
            return "0 B";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%.2f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    /**
     * 从JDBC URL中解析主机地址
     */
    private String parseHostFromUrl(String url) {
        if (StrUtil.isBlank(url)) {
            return "localhost";
        }
        try {
            String cleanUrl = url.substring(url.indexOf("://") + 3);
            int queryIndex = cleanUrl.indexOf("?");
            if (queryIndex > 0) {
                cleanUrl = cleanUrl.substring(0, queryIndex);
            }
            int lastSlashIndex = cleanUrl.lastIndexOf("/");
            if (lastSlashIndex > 0) {
                cleanUrl = cleanUrl.substring(0, lastSlashIndex);
            }
            int colonIndex = cleanUrl.indexOf(":");
            return colonIndex > 0 ? cleanUrl.substring(0, colonIndex) : cleanUrl;
        } catch (Exception e) {
            return "localhost";
        }
    }

    /**
     * 从JDBC URL中解析端口
     */
    private Integer parsePortFromUrl(String url) {
        if (StrUtil.isBlank(url)) {
            return 3306;
        }
        try {
            String cleanUrl = url.substring(url.indexOf("://") + 3);
            int queryIndex = cleanUrl.indexOf("?");
            if (queryIndex > 0) {
                cleanUrl = cleanUrl.substring(0, queryIndex);
            }
            int lastSlashIndex = cleanUrl.lastIndexOf("/");
            if (lastSlashIndex > 0) {
                cleanUrl = cleanUrl.substring(0, lastSlashIndex);
            }
            int colonIndex = cleanUrl.indexOf(":");
            if (colonIndex > 0) {
                String portStr = cleanUrl.substring(colonIndex + 1);
                return Integer.parseInt(portStr);
            }
            return 3306;
        } catch (Exception e) {
            return 3306;
        }
    }

    /**
     * 从JDBC URL中解析数据库名
     */
    private String parseDbNameFromUrl(String url) {
        if (StrUtil.isBlank(url)) {
            return "";
        }
        try {
            String cleanUrl = url.substring(url.indexOf("://") + 3);
            int queryIndex = cleanUrl.indexOf("?");
            if (queryIndex > 0) {
                cleanUrl = cleanUrl.substring(0, queryIndex);
            }
            int lastSlashIndex = cleanUrl.lastIndexOf("/");
            if (lastSlashIndex > 0) {
                return cleanUrl.substring(lastSlashIndex + 1);
            }
            return "";
        } catch (Exception e) {
            return "";
        }
    }
}
