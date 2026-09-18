package com.budaos.support.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 系统更新日志实体
 *
 * @author budaos
 */
@Data
@TableName("t_change_log")
public class ReleaseLogEntity {

    @TableId(type = IdType.AUTO)
    private Long changeLogId;

    /**
     * 版本
     */
    private String updateVersion;

    /**
     * 更新类型:[1:特大版本功能更新;2:功能更新;3:bug修复]
     */
    private Integer type;

    /**
     * 发布人
     */
    private String publishAuthor;

    /**
     * 发布日期
     */
    private LocalDate publicDate;

    /**
     * 更新内容
     */
    private String content;

    /**
     * 跳转链接
     */
    private String link;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
