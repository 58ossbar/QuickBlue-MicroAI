package com.budaos.support.constant;

import com.budaos.common.core.domain.BaseEnumeration;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 单据序列号 枚举
 *
 * @author budaos
 */
@AllArgsConstructor
@Getter
public enum SerialCodeIdEnum implements BaseEnumeration {

    BATCH_QuickBlue(1, "批次编号"),

    QuickBlue(2, "票券编号");

    private final Integer value;

    private final String desc;

    /**
     * 获取序列号ID
     */
    public Integer getSerialNumberId() {
        return this.value;
    }
}
