package com.budaos.support.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据字典 实体类
 *
 * @author budaos
 */
@Data
@TableName("t_dict")
@Schema(description = "数据字典实体")
public class DictionaryEntity {

    /**
     * 字典id
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "字典id")
    private Long dictId;

    /**
     * 字典名字
     */
    @Schema(description = "字典名字")
    private String dictName;

    /**
     * 字典编码
     */
    @Schema(description = "字典编码")
    private String dictCode;

    /**
     * 字典备注
     */
    @Schema(description = "字典备注")
    private String remark;

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
