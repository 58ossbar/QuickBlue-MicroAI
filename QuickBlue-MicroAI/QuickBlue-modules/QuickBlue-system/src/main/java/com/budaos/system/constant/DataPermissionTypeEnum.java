package com.budaos.system.constant;

/**
 * 数据权限类型枚举
 *
 * @author budaos
 * @since 2026-02-11
 */
public enum DataPermissionTypeEnum {

    ALL(1, "全部数据权限", 1),

    CUSTOM(2, "自定义数据权限", 2),

    DEPT(3, "本部门数据权限", 3),

    DEPT_AND_CHILD(4, "本部门及以下数据权限", 4),

    ONLY_SELF(5, "仅本人数据权限", 5),

    NOTICE(6, "通知权限", 6),

    EMPLOYEE(7, "员工管理", 7),
    ;

    private final Integer value;

    private final String desc;

    /**
     * 排序值
     */
    private final int sort;

    DataPermissionTypeEnum(Integer value, String desc, int sort) {
        this.value = value;
        this.desc = desc;
        this.sort = sort;
    }

    public Integer getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    public int getSort() {
        return sort;
    }

    public String getName() {
        return desc;
    }
}
