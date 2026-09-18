package com.budaos.support.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 单据序列号 定义表
 *
 * @author budaos
 */
@Data
@TableName("t_serial_number")
@Schema(description = "单据序列号定义表实体")
public class SerialCodeEntity {

    /**
     * 主键id
     *
     * @see com.budaos.support.constant.SerialCodeIdEnum
     */
    @TableId(type = IdType.INPUT)
    @Schema(description = "序列号ID")
    private Integer serialNumberId;

    /**
     * 业务
     */
    @Schema(description = "业务名称")
    private String businessName;

    /**
     * 格式
     */
    @Schema(description = "格式")
    private String format;

    /**
     * 生成规则
     *
     * @see com.budaos.support.constant.SerialCodeRuleTypeEnum
     */
    @Schema(description = "生成规则类型")
    private String ruleType;

    /**
     * 初始值
     */
    @Schema(description = "初始值")
    private Long initNumber;

    /**
     * 步长随机数范围
     */
    @Schema(description = "步长随机数范围")
    private Integer stepRandomRange;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

    /**
     * 安全模式
     *
     * @see com.budaos.support.constant.SerialCodeSecureModeEnum
     */
    @Schema(description = "安全模式")
    private Integer secureMode;

    /**
     * 随机跳跃范围
     */
    @Schema(description = "随机跳跃范围")
    private Integer randomRange;

    /**
     * 加密密钥
     */
    @Schema(description = "加密密钥")
    private String encryptKey;

    /**
     * 上次产生的单号, 默认为空
     */
    @Schema(description = "上次产生的单号")
    private Long lastNumber;

    /**
     * 上次产生的单号时间
     */
    @Schema(description = "上次产生的单号时间")
    private LocalDateTime lastTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
