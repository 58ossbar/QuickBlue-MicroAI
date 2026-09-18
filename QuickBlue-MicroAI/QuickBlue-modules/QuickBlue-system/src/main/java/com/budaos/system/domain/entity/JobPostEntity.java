package com.budaos.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 职务表实体类
 *
 * @author budaos
 */
@Data
@TableName("t_position")
public class JobPostEntity {

    /**
     * 职务ID
     */
    @TableId(type = IdType.AUTO)
    private Long positionId;

    /**
     * 职务名称
     */
    private String positionName;

    /**
     * 岗位编码
     */
    private String positionCode;

    /**
     * 岗位类别
     */
    private String category;

    /**
     * 职级
     */
    private String positionLevel;

    /**
     * 职级等级
     */
    private Integer gradeLevel;

    /**
     * 父级岗位ID
     */
    private Long parentId;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态（1启用 0停用）
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 岗位职责描述
     */
    private String description;

    /**
     * 删除标记
     */
    private Boolean deletedFlag;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}
