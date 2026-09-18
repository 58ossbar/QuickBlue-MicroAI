package com.budaos.support.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 邮件模板实体
 *
 * @author budaos
 */
@Data
@TableName("t_mail_template")
public class EmailTemplateEntity {

    @TableId(type = IdType.NONE)
    private String templateCode;

    /**
     * 主题
     */
    private String templateSubject;

    /**
     * 模板类型
     */
    private String templateType;

    /**
     * 模板内容
     */
    private String templateContent;

    /**
     * 禁用标识
     */
    private Boolean disableFlag;

    private LocalDateTime updateTime;

    private LocalDateTime createTime;
}
