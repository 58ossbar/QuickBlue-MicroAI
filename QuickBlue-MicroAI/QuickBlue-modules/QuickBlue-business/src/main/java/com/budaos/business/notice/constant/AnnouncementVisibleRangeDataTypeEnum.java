package com.budaos.business.notice.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 公告、通知 可见范围类型
 */
@Getter
@AllArgsConstructor
public enum AnnouncementVisibleRangeDataTypeEnum {

    /**
     * 员工
     */
    EMPLOYEE(1, "员工"),

    /**
     * 部门
     */
    DEPARTMENT(2, "部门"),
    ;

    private final Integer value;

    private final String desc;

    public static AnnouncementVisibleRangeDataTypeEnum getByValue(Integer value) {
        for (AnnouncementVisibleRangeDataTypeEnum enumItem : AnnouncementVisibleRangeDataTypeEnum.values()) {
            if (enumItem.getValue().equals(value)) {
                return enumItem;
            }
        }
        return null;
    }

    public boolean equalsValue(Integer value) {
        return this.value != null && this.value.equals(value);
    }
}
