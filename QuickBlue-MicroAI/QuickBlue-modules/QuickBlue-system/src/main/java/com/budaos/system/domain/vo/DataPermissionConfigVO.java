package com.budaos.system.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据权限配置VO
 *
 * @author budaos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataPermissionConfigVO {

    /**
     * 配置ID
     */
    private Long configId;

    /**
     * 配置编码
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
     * 状态
     */
    private Boolean status;

    /**
     * 创建时间
     */
    private String createTime;

}
