package com.budaos.support.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 帮助文档的 类型
 *
 * @author budaos
 */
@Data
@TableName("t_help_doc_catalog")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HelpCenterCatalogEntity {

    @TableId(type = IdType.AUTO)
    private Long helpDocCatalogId;

    /**
     * 名称
     */
    private String name;

    /**
     * 父id
     */
    private Long parentId;

    /**
     * 排序
     */
    private Integer sort;

    private LocalDateTime updateTime;

    private LocalDateTime createTime;
}
