package com.budaos.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * job 任务触发类型 枚举类
 *
 * @author budaos
 **/
@AllArgsConstructor
@Getter
public enum JobTaskTriggerTypeEnum {

    /**
     * 1 cron表达式
     */
    CRON("cron", "cron表达式"),

    FIXED_DELAY("fixed_delay", "固定间隔"),

    ;

    private final String value;
    private final String desc;

    public boolean equalsValue(String value) {
        return this.value.equals(value);
    }
}
