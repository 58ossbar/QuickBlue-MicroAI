package com.budaos.common.core.domain;

import com.google.common.base.CaseFormat;

import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * 枚举类接口
 */
public interface BaseEnumeration {

    /**
     * 获取枚举类的值
     *
     * @return
     */
    Object getValue();

    /**
     * 获取枚举类的说明
     *
     * @return String
     */
    String getDesc();

    /**
     * 比较参数是否与枚举类的value相同
     *
     * @param value
     * @return boolean
     */
    default boolean equalsValue(Object value) {
        return Objects.equals(getValue(), value);
    }

    /**
     * 比较枚举类是否相同
     *
     * @param baseEnum
     * @return boolean
     */
    default boolean equals(BaseEnumeration baseEnum) {
        return Objects.equals(getValue(), baseEnum.getValue()) && Objects.equals(getDesc(), baseEnum.getDesc());
    }
}
