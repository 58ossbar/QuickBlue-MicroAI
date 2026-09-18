package com.budaos.system.constant;

/**
 * 菜单权限类型枚举
 *
 * @author budaos
 * @since 2026-02-11
 */
public enum NavMenuPermsTypeEnum {

    BUTTON(1, "按钮"),

    MENU(2, "菜单"),
    ;

    private final Integer value;

    private final String desc;

    NavMenuPermsTypeEnum(Integer value, String desc) {
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
