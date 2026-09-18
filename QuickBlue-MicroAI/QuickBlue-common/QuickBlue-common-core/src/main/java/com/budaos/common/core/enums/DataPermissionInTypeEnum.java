package com.budaos.common.core.enums;

/**
 * 数据权限WhereIn类型枚举
 *
 * @author budaos
 * @since 2026-02-11
 */
public enum DataPermissionInTypeEnum {

    /**
     * 部门类型 - 拼接部门ID列表
     */
    DEPARTMENT(1, "部门"),

    /**
     * 员工类型 - 拼接员工ID列表
     */
    EMPLOYEE(2, "员工"),

    /**
     * 自定义策略 - 使用自定义策略生成SQL条件
     */
    CUSTOM_STRATEGY(3, "自定义策略"),
    ;

    private final Integer value;

    private final String desc;

    DataPermissionInTypeEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public Integer getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }
}
