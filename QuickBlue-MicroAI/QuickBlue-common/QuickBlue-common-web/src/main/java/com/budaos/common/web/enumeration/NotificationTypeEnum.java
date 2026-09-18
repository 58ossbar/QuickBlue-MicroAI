package com.budaos.common.web.enumeration;

import com.budaos.common.core.domain.BaseEnumeration;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息类型
 *
 * @author budaos
 * @since 2026-02-11
 */
@Getter
@AllArgsConstructor
public enum NotificationTypeEnum implements BaseEnumeration {

    MAIL(1, "站内信"),

    ORDER(2, "订单"),
    ;

    private final Integer value;

    private final String desc;
}
