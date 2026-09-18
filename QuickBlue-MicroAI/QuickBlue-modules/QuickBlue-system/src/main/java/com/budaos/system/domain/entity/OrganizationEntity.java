package com.budaos.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 部门实体
 *
 * @author budaos
 */
@Data
@TableName("t_department")
public class OrganizationEntity {

    @TableId(type = IdType.AUTO)
    private Long departmentId;

    /**
     * 部门名称
     */
    private String departmentName;

    /**
     * 负责人员工 id
     */
    private Long managerId;

    /**
     * 部门父级id
     */
    private Long parentId;

    /**
     * 排序
     */
    private Integer sort;

    private LocalDateTime updateTime;

    private LocalDateTime createTime;

}
