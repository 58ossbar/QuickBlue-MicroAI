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
 * Nacos配置变更审计实体
 *
 * @author budaos
 * @since 2026-02-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_sys_config_audit")
public class SysConfigAuditEntity {

    /**
     * 主键ID
     */
    @TableId(value = "audit_id", type = IdType.AUTO)
    private Long auditId;

    /**
     * 配置ID
     */
    private String dataId;

    /**
     * 配置分组
     */
    private String groupId;

    /**
     * 命名空间ID
     */
    private String tenantId;

    /**
     * 配置名称(用于展示)
     */
    private String configName;

    /**
     * 修改前内容
     */
    private String oldContent;

    /**
     * 修改后内容
     */
    private String newContent;

    /**
     * 操作类型: CREATE/UPDATE/DELETE
     */
    private String opType;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 操作人名称
     */
    private String operatorName;

    /**
     * 操作IP
     */
    private String operatorIp;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
