package com.budaos.support.nacos.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Nacos配置模板实体
 *
 * @author budaos
 * @since 2026-02-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_nacos_config_template")
public class NacosConfigTemplateEntity {

    /**
     * 模板主键ID
     */
    @TableId(value = "template_id", type = IdType.AUTO)
    private Long templateId;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 模板编码(唯一标识)
     */
    private String templateCode;

    /**
     * 配置ID模板
     */
    private String dataId;

    /**
     * 配置分组模板
     */
    private String groupId;

    /**
     * 配置内容模板
     */
    private String content;

    /**
     * 配置类型: yaml/properties/text
     */
    private String type;

    /**
     * 数据库类型: common/mysql/postgresql
     */
    private String databaseType;

    /**
     * 模板描述
     */
    private String description;

    /**
     * 创建人ID
     */
    private Long createUserId;

    /**
     * 创建人姓名
     */
    private String createUserName;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
