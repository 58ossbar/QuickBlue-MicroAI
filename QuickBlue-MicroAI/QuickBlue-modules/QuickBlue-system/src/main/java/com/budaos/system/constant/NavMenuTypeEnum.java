package com.budaos.system.constant;

/**
 * 菜单类型枚举
 *
 * @author budaos
 * @since 2026-02-11
 */
public enum NavMenuTypeEnum {

    CATALOG(1, "目录"),

    MENU(2, "菜单"),

    BUTTON(3, "按钮"),
    ;

    private final Integer value;

    private final String desc;

    NavMenuTypeEnum(Integer value, String desc) {
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
