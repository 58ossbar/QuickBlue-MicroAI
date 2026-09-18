package com.budaos.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据权限配置实体
 *
 * @author budaos
 */
@Data
@TableName("t_data_scope_config")
public class DataPermissionConfigEntity {

    @TableId(type = IdType.AUTO)
    private Long configId;

    /**
     * 配置编码（用于代码中引用）
     */
    private String configCode;

    /**
     * 配置名称
     */
    private String configName;

    /**
     * 配置描述
     */
    private String configDesc;

    /**
     * 业务模块名称
     */
    private String businessModule;

    /**
     * 默认视图类型
     */
    private Integer defaultViewType;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 状态（0-禁用，1-启用）
     */
    private Boolean status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新人
     */
    private String updateBy;

}
