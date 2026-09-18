/*
 * database backup api
 *
 */
import { postRequest, getRequest, getDownload } from '/@/lib/axios';

export const databaseBackupApi = {
    // 分页查询备份记录
    queryPage: (param) => {
        return postRequest('/support/database-backup/queryPage', param);
    },
    // 手动备份
    manualBackup: (param) => {
        return postRequest('/support/database-backup/manualBackup', param);
    },
    // 删除备份记录
    delete: (backupId) => {
        return postRequest(`/support/database-backup/delete?backupId=${backupId}`);
    },
    // 查询备份配置
    getConfig: () => {
        return getRequest('/support/database-backup/config/get');
    },
    // 更新备份配置
    updateConfig: (param) => {
        return postRequest('/support/database-backup/config/update', param);
    },
    // 清理过期备份
    cleanExpired: () => {
        return postRequest('/support/database-backup/cleanExpired');
    },
    // 获取自动备份任务类名
    getAutoBackupJobClass: () => {
        return getRequest('/support/database-backup/autoBackupJobClass');
    },
    // 下载备份文件
    downloadBackup: (backupId) => {
        getDownload('/support/database-backup/download', { backupId });
    },
};
