package com.budaos.support.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据库备份实体
 *
 * @author budaos
 */
@Data
@TableName("t_database_backup")
public class DatabaseBackupEntity {

    @TableId(type = IdType.AUTO)
    private Long backupId;

    /**
     * 备份文件名
     */
    private String fileName;

    /**
     * 备份文件路径
     */
    private String filePath;

    /**
     * 备份文件大小(字节)
     */
    private Long fileSize;

    /**
     * 备份类型(1-自动备份 2-手动备份)
     */
    private Integer backupType;

    /**
     * 备份状态(0-备份中 1-备份成功 2-备份失败)
     */
    private Integer backupStatus;

    /**
     * 备份描述/备注
     */
    private String remark;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
