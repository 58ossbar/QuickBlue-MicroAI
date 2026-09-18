package com.budaos.system.common.enums;

import com.budaos.common.core.domain.BaseEnumeration;

/**
 * 用户类型
 *

 */
public enum AccountTypeEnum implements BaseEnumeration {

    /**
     * 管理端 员工用户
     */
    ADMIN_EMPLOYEE(1, "员工");

    private Integer type;

    private String desc;

    AccountTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    @Override
    public Integer getValue() {
        return type;
    }

    @Override
    public String getDesc() {
        return desc;
    }
}
