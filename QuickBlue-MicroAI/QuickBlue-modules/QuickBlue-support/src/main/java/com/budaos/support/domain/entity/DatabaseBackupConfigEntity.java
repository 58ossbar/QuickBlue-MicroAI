package com.budaos.support.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据库备份配置实体
 *
 * @author budaos
 */
@Data
@TableName("t_database_backup_config")
public class DatabaseBackupConfigEntity {

    @TableId(type = IdType.AUTO)
    private Long configId;

    /**
     * 数据库主机
     */
    private String dbHost;

    /**
     * 数据库端口
     */
    private Integer dbPort;

    /**
     * 数据库名称
     */
    private String dbName;

    /**
     * 数据库用户名
     */
    private String dbUsername;

    /**
     * 数据库密码(加密)
     */
    private String dbPassword;

    /**
     * 备份文件存储路径
     */
    private String backupPath;

    /**
     * mysqldump命令路径(可选，如果不填则自动查找)
     */
    private String mysqldumpPath;

    /**
     * 是否启用自动备份(0-否 1-是)
     */
    private Boolean autoBackupEnabled;

    /**
     * 自动备份cron表达式
     */
    private String autoBackupCron;

    /**
     * 备份保留天数
     */
    private Integer retentionDays;

    /**
     * 备注
     */
    private String remark;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
