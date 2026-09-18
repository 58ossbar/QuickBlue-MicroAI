package com.budaos.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据类型枚举
 */
@Getter
@AllArgsConstructor
public enum ApiDataTypeEnum {
    NORMAL(0, "正常");

    private final Integer value;
    private final String desc;
}
