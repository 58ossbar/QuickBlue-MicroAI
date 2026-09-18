package com.budaos.business.region.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.budaos.common.core.domain.BaseEntity;
import lombok.Data;

/**
 * 区域表（树形结构） 实体类
 *
 * @Author zhujw
 * @Date 2025-12-20 14:34:26
 * @Copyright v1.0
 */

@Data
@TableName("t_region")
public class RegionEntity extends BaseEntity {

    /**
     * 区域ID
     */
    @TableId
    private String id;

    /**
     * 区域编码（唯一）
     */
    private String regionCode;

    /**
     * 区域名称
     */
    private String regionName;

    /**
     * 区域简称
     */
    private String regionShortName;

    /**
     * 父级区域ID
     */
    private String parentId;

    /**
     * 父级路径（格式: /id1/id2/id3）
     */
    private String parentPath;

    /**
     * 区域层级：1-大区 2-省/市 3-城市 4-区县
     */
    private Integer level;

    /**
     * 排序（同层级内）
     */
    private Integer sortOrder;

    /**
     * 状态：1-启用 2-禁用
     */
    private Integer status;


    /**
     * 区域负责人姓名
     */
    private String regionManagerName;

    /**
     * 联系电话
     */
    private String contactPhone;


    /**
     * 是否叶子节点：0-否 1-是
     */
    private Integer leafFlag;


}
