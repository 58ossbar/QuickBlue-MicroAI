package com.budaos.support.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 单据序列号 表结构
 *
 * @author budaos
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_serial_number_record")
@Schema(description = "单据序列号记录表实体")
public class SerialCodeRecordEntity {

    /**
     * 单号id
     */
    @TableId(type = IdType.NONE)
    @Schema(description = "序列号ID")
    private Integer serialNumberId;

    /**
     * 记录日期
     */
    @Schema(description = "记录日期")
    private LocalDate recordDate;

    /**
     * 最后更新值
     */
    @Schema(description = "最后更新值")
    private Long lastNumber;

    /**
     * 原始序列号（安全模式使用）
     */
    @Schema(description = "原始序列号")
    private Long originalSequence;

    /**
     * 上次生成时间
     */
    @Schema(description = "上次生成时间")
    private LocalDateTime lastTime;

    /**
     * 数量
     */
    @Schema(description = "数量")
    private Long count;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
