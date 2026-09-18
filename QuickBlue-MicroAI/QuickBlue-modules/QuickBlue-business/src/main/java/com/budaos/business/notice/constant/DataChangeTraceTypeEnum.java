package com.budaos.business.notice.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据追踪类型枚举
 */
@Getter
@AllArgsConstructor
public enum DataChangeTraceTypeEnum {

    /**
     * 创建
     */
    CREATE(1, "创建"),

    /**
     * 更新
     */
    UPDATE(2, "更新"),

    /**
     * 删除
     */
    DELETE(3, "删除"),

    /**
     * OA通知
     */
    OA_NOTICE(4, "OA通知"),

    ;

    private final Integer value;

    private final String desc;
}
