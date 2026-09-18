package com.budaos.support.service;

import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.support.domain.entity.DatabaseBackupEntity;
import com.budaos.support.domain.form.DatabaseBackupAddForm;
import com.budaos.support.domain.form.DatabaseBackupConfigUpdateForm;
import com.budaos.support.domain.form.DatabaseBackupQueryForm;
import com.budaos.support.domain.vo.DatabaseBackupConfigVO;
import com.budaos.support.domain.vo.DatabaseBackupVO;

/**
 * 数据库备份服务
 *
 * @author budaos
 */
public interface DatabaseBackupService {

    /**
     * 分页查询备份记录
     *
     * @param queryForm 查询表单
     * @return 分页结果
     */
    ApiResult<PageResponse<DatabaseBackupVO>> queryBackupPage(DatabaseBackupQueryForm queryForm);

    /**
     * 根据ID获取备份记录
     *
     * @param backupId 备份ID
     * @return 备份实体
     */
    DatabaseBackupEntity getBackupById(Long backupId);

    /**
     * 手动备份
     *
     * @param addForm    备份表单
     * @param requestUser 请求用户
     * @return 备份结果
     */
    ApiResult<String> manualBackup(DatabaseBackupAddForm addForm, String requestUser);

    /**
     * 删除备份记录
     *
     * @param backupId 备份ID
     * @return 删除结果
     */
    ApiResult<String> deleteBackup(Long backupId);

    /**
     * 查询备份配置
     *
     * @return 配置VO
     */
    ApiResult<DatabaseBackupConfigVO> getConfig();

    /**
     * 更新备份配置
     *
     * @param updateForm 更新表单
     * @return 更新结果
     */
    ApiResult<String> updateConfig(DatabaseBackupConfigUpdateForm updateForm);

    /**
     * 清理过期备份
     *
     * @return 清理结果
     */
    ApiResult<String> cleanExpiredBackups();
}
