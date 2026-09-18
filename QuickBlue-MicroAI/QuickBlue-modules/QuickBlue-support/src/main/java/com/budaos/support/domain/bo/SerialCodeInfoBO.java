package com.budaos.support.domain.bo;

import com.budaos.support.constant.SerialCodeRuleTypeEnum;
import com.budaos.support.constant.SerialCodeSecureModeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 单据序列号 信息
 *
 * @author budaos
 */

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SerialCodeInfoBO {

    /**
     * 主键id
     */
    private Integer serialNumberId;

    /**
     * 业务
     */
    private String businessName;

    /**
     * 格式
     */
    private String format;

    /**
     * 生成规则
     */
    private String ruleType;

    /**
     * 初始值
     */
    private Long initNumber;

    /**
     * 步长随机数范围
     */
    private Integer stepRandomRange;

    /**
     * 备注
     */
    private String remark;

    /**
     * 安全模式
     */
    private Integer secureMode;

    /**
     * 随机跳跃范围
     */
    private Integer randomRange;

    /**
     * 加密密钥
     */
    private String encryptKey;

    /**
     * 规则枚举
     */
    private SerialCodeRuleTypeEnum serialNumberRuleTypeEnum;

    /**
     * 安全模式枚举
     */
    private SerialCodeSecureModeEnum serialNumberSecureModeEnum;

    /**
     * 数字位数
     */
    private Integer numberCount;

    /**
     * 数字格式的正则表达式（用于替换）
     */
    private String numberFormat;

    /**
     * 前缀（数字占位符前的固定字符）
     */
    private String prefix;

    /**
     * 后缀（数字占位符后的固定字符）
     */
    private String suffix;

    /**
     * 是否存在年份
     */
    private Boolean haveYearFlag;

    /**
     * 是否存在月份
     */
    private Boolean haveMonthFlag;

    /**
     * 是否存在日
     */
    private Boolean haveDayFlag;

    /**
     * 年份格式
     */
    private String yearFormat;

    /**
     * 月份格式
     */
    private String monthFormat;

    /**
     * 日格式
     */
    private String dayFormat;

    /**
     * 年份正则
     */
    private String yearRegex;

    /**
     * 月份正则
     */
    private String monthRegex;

    /**
     * 日正则
     */
    private String dayRegex;

}
