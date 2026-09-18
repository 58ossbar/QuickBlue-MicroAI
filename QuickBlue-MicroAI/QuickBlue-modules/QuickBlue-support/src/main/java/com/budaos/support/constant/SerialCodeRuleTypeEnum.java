package com.budaos.support.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 单据序列号 周期
 *
 * @author budaos
 */
@AllArgsConstructor
@Getter
public enum SerialCodeRuleTypeEnum {
    /**
     * 没有周期
     */
    NONE("", "", "没有周期"),
    /**
     * 年周期
     */
    YEAR("[yyyy]", "\\[yyyy\\]", "年"),
    /**
     * 月周期
     */
    MONTH("[mm]", "\\[mm\\]", "年月"),
    /**
     * 日周期
     */
    DAY("[dd]", "\\[dd\\]", "年月日");

    private final String value;

    private final String regex;

    private final String desc;
}
