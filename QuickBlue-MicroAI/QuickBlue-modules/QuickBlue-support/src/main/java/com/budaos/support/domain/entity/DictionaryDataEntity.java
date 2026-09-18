package com.budaos.support.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 字典数据表 实体类
 *
 * @author budaos
 */
@Data
@TableName("t_dict_data")
@Schema(description = "字典数据表实体")
public class DictionaryDataEntity {

    /**
     * 字典数据id
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "字典数据id")
    private Long dictDataId;

    /**
     * 字典id
     */
    @Schema(description = "字典id")
    private Long dictId;

    /**
     * 字典项值
     */
    @Schema(description = "字典项值")
    private String dataValue;

    /**
     * 字典项显示名称
     */
    @Schema(description = "字典项显示名称")
    private String dataLabel;

    /**
     * 父级编码（用于树形字典）
     */
    @Schema(description = "父级编码（用于树形字典）")
    private String parentCode;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

    /**
     * 排序（越大越靠前）
     */
    @Schema(description = "排序（越大越靠前）")
    private Integer sortOrder;

    /**
     * 禁用状态
     */
    @Schema(description = "禁用状态")
    private Boolean disabledFlag;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

}
