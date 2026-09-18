package com.budaos.system.constant;

/**
 * 数据权限视图类型枚举
 *
 * @author budaos
 * @since 2026-02-11
 */
public enum DataPermissionViewTypeEnum {

    ME(0, "仅本人"),

    DEPARTMENT(1, "部门"),

    DEPARTMENT_AND_SUB(2, "本部门及以下"),

    ALL(3, "全部数据"),
    ;

    private final Integer value;

    private final String desc;

    DataPermissionViewTypeEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public Integer getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 权限级别（数字越大，权限越大）
     */
    public int getLevel() {
        return value;
    }

    /**
     * 根据值获取枚举
     *
     * @param value 值
     * @return 枚举
     */
    public static DataPermissionViewTypeEnum getEnumByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (DataPermissionViewTypeEnum enumItem : DataPermissionViewTypeEnum.values()) {
            if (enumItem.getValue().equals(value)) {
                return enumItem;
            }
        }
        return null;
    }
}
